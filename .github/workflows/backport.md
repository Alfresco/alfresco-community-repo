---
name: Backport to release branch
emoji: "🔀"
description: Cherry-picks commits from master onto a target release branch, resolves any cherry-pick conflicts, and opens a Jira-prefixed PR.
on:
  workflow_dispatch:
    inputs:
      target_branch:
        description: "Branch to backport onto (e.g. release/25.N, release/25.4, release/23.7)"
        type: string
        required: true
      commits:
        description: "Commit SHAs to cherry-pick, oldest first (space/comma/newline separated, full or short)"
        type: string
        required: true
      jira_ticket:
        description: "Jira ticket ID to prefix the PR title with (e.g. ACS-1234)"
        type: string
        required: true
permissions:
  contents: read
  pull-requests: read
  issues: read
strict: true
timeout-minutes: 30
tools:
  bash: ["git:*"]
  edit:
  github:
    mode: gh-proxy
    toolsets: [default]
safe-outputs:
  create-pull-request:
    max: 1
    allowed-base-branches:
        - "release/*"
  noop:
runtimes:
  python:
    version: 3.11
---

# Backport to Release Branch

## Current Context

- **Repository**: ${{ github.repository }}
- **Run**: ${{ github.run_id }}
- **Jira ticket**: ${{ github.event.inputs.jira_ticket }}
- **Target branch**: ${{ github.event.inputs.target_branch }}
- **Commits to cherry-pick, in this exact order**: ${{ github.event.inputs.commits }}

## Task

You are backporting a fixed list of commits, which live on `master`, onto the target branch above, by creating a new branch **from the target branch** and opening a single pull request **back to the target branch**. The flow, in order, is:

```
create a branch from <target_branch> → apply the cherry-picked commits → resolve conflicts → open a PR to <target_branch>
```

`master` is only where the commit SHAs are looked up and validated in step 1 below. You must never check out `master`, merge it, rebase onto it, or otherwise bring its tree into the backport branch as a whole — the only content that may enter the backport branch from `master` is the diff introduced by each individual cherry-picked commit in step 4.

1. **Resolve and validate the commit list against `master`.**
   - Normalize the `commits` input into an ordered list: replace every comma and newline with a space, split on whitespace, and drop empty tokens. Keep the resulting tokens in the exact order they were supplied — this is the cherry-pick order.
   - For each token, resolve it to a full 40-character SHA with `git rev-parse --verify <token>^{commit}`, then confirm it is actually reachable from master with `git merge-base --is-ancestor <resolved-sha> origin/master`.
   - If normalization yields an empty list, or any token fails to resolve or is not an ancestor of `origin/master`, call `noop` naming exactly which token(s) failed, and stop. Do not silently drop, reorder, or guess at a bad token.

2. **Validate the target branch.** Confirm `origin/${{ github.event.inputs.target_branch }}` exists with `git rev-parse --verify --quiet origin/${{ github.event.inputs.target_branch }}`. If it does not exist, call `noop` explaining the branch was not found, and stop.

3. **Create the backport branch FROM the target branch.** Make sure your working tree is clean (`git status --porcelain`), then branch from the target branch's current remote tip — not from master, not from whatever branch you happen to be on:
   ```
   git checkout -B backport/<target-branch-with-slashes-replaced-by-dashes>/${{ github.run_id }} origin/${{ github.event.inputs.target_branch }}
   ```
   Immediately after creating it, verify the branch point is correct: `git merge-base HEAD origin/${{ github.event.inputs.target_branch }}` must equal `git rev-parse origin/${{ github.event.inputs.target_branch }}`. If it does not, you branched from the wrong base — stop and call `noop` rather than proceeding.

4. **Apply the cherry-picked commits.** On top of this new branch, cherry-pick each SHA resolved in step 1, in order, with `git cherry-pick -x <sha>`.

5. **Resolve conflicts.** If a cherry-pick reports a conflict: open every file `git diff --name-only --diff-filter=U` lists, read both sides of each conflict marker, and hand-edit the file with the `edit` tool so the result preserves the intent of the original commit while fitting the current state of the target branch. Do not blindly prefer "ours" or "theirs". After resolving, `git add` the files and run `git cherry-pick --continue` (set `GIT_EDITOR=true` so it doesn't wait on an interactive commit-message prompt). If a commit is a pure no-op on this branch (already present / empty diff), skip it with `git cherry-pick --skip` and note that in the PR body.

6. **Open a PR to the target branch.** Once every commit has been applied (cleanly or with resolved conflicts), use the `create_pull_request` tool:
   - `base`: `${{ github.event.inputs.target_branch }}`
   - `title`: `"[${{ github.event.inputs.jira_ticket }}] Backport: <one-line summary> (master → ${{ github.event.inputs.target_branch }})"` — always start with the Jira ticket in brackets.
   - `body`: list every SHA that was cherry-picked (short form, `git rev-parse --short`), in order; for any commit where you had to resolve a conflict, describe what the conflict was and how you resolved it; for any commit skipped as a no-op, say so explicitly.

7. **Do not push directly or call the GitHub API to open the PR yourself** — pushing the branch and opening the PR is handled automatically by the `create_pull_request` safe output once you call it; you are only responsible for the local git history on the backport branch.

## Guidelines

- The backport branch must start from the target branch's tip (step 3). If you ever find yourself on `master` or on a branch whose merge-base with `origin/${{ github.event.inputs.target_branch }}` is not that branch's own tip, stop — do not attempt to fix it by merging or rebasing; call `noop` and explain what went wrong.
- Preserve commit authorship and messages via `-x` (adds a `(cherry picked from commit ...)` trailer) — do not squash or reword unless resolving a conflict requires touching the same lines.
- Be conservative when resolving conflicts: if you cannot confidently reconcile a conflict without changing behavior, leave the conflict markers in place, commit them as-is, and clearly flag in the PR body which files still need human review — do not guess silently.
- Never modify files outside the ones touched by the cherry-picked commits.

## Safe Outputs

- **create_pull_request**: exactly one call, per the Task section above.
- **noop**: use when the target branch doesn't exist, no commits were provided, a commit SHA fails to resolve or isn't an ancestor of master, or the backport branch was created from the wrong base — with a one-line reason.
