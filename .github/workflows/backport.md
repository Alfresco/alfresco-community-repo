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
checkout:
  - fetch-depth: 0
    fetch: ["release/*"]
permissions:
  contents: read
  pull-requests: read
  issues: read
strict: true
timeout-minutes: 30
network:
  allowed:
    - defaults
    - python
tools:
  bash: ["git:*", "pip:*", "detect-secrets:*"]
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

0. **Confirm the checkout has full history before doing anything else.** Run `git rev-parse --is-shallow-repository`. It must print `false`. If it prints `true`, the repository is shallow-cloned and any diff you produce against `origin/${{ github.event.inputs.target_branch }}` will be meaningless (it will appear to touch every file in the repository). Do **not** attempt to work around this yourself with `git fetch --unshallow` or similar — git credentials are intentionally removed after checkout and that fetch will fail anyway. Instead call `noop` reporting that the checkout is shallow and that the workflow's `checkout:` frontmatter needs `fetch-depth: 0`, and stop.

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

4. **Apply the cherry-picked commits.** Before cherry-picking anything, run `git config core.editor true` on this checkout so no cherry-pick or continue ever blocks waiting on an interactive commit-message editor. Then, for each SHA resolved in step 1, in order:
   a. Run `git cherry-pick -x <sha>`.
   b. If it conflicts, resolve per step 5 below (note the special case for `.secrets.baseline`) and continue with `git cherry-pick --continue`. If it's a pure no-op on this branch, skip it with `git cherry-pick --skip` and note that in the PR body.
   c. **Regardless of whether this commit applied cleanly or needed conflict resolution**, regenerate the secrets baseline immediately afterward — see "Secrets baseline (`.secrets.baseline`)" below — before moving on to the next SHA.

5. **Resolve conflicts.** If a cherry-pick reports a conflict: open every file `git diff --name-only --diff-filter=U` lists, read both sides of each conflict marker, and hand-edit the file with the `edit` tool so the result preserves the intent of the original commit while fitting the current state of the target branch. Do not blindly prefer "ours" or "theirs" — **except for `.secrets.baseline`, which is handled differently, see below.** After resolving, `git add` the files and run `git cherry-pick --continue`.

   **Secrets baseline (`.secrets.baseline`)**: This repo maintains `.secrets.baseline` as a generated artifact of `detect-secrets` (used by the pre-commit secret-scanning hook) — it is never meant to be hand-edited or hand-merged.
   - If `.secrets.baseline` shows up as conflicted in `git diff --name-only --diff-filter=U`, do not read or reconcile its conflict markers at all. Just pick either side arbitrarily to unblock the cherry-pick, e.g. `git checkout --ours .secrets.baseline && git add .secrets.baseline` (either `--ours` or `--theirs` is fine — the exact content doesn't matter because it's about to be regenerated).
   - After **every** cherry-pick — clean or conflicted, whether or not `.secrets.baseline` was touched — regenerate it from scratch so it always reflects the branch's actual current content:
     1. If the `detect-secrets` command is not already on `PATH`, install it once with `pip install --quiet detect-secrets`.
     2. Run `detect-secrets scan > .secrets.baseline` from the repository root.
     3. Run `git status --porcelain -- .secrets.baseline`. If it reports a change, `git add .secrets.baseline` and fold it into the commit you just made with `git commit --amend --no-edit` (never leave it as a separate trailing commit).
   - Do not describe `.secrets.baseline` conflicts or diffs in the PR body beyond noting that it was regenerated — it's routine bookkeeping, not a real conflict resolution worth reviewer attention.

6. **Verify the final backport diff, then open a PR to the target branch.** Before calling `create_pull_request`, you must prove the branch is still a small backport branch based on the target branch.

   Run all of the following checks:

    - `git merge-base HEAD origin/${{ github.event.inputs.target_branch }}` must equal `git rev-parse origin/${{ github.event.inputs.target_branch }}`
    - `git diff --name-only origin/${{ github.event.inputs.target_branch }}...HEAD` must list only files touched by the cherry-picked commits and any files unavoidably changed while resolving conflicts
    - `git diff --name-only origin/${{ github.event.inputs.target_branch }}...HEAD | wc -l` must be **100 or less**

   If any of these checks fail, call `noop` with a clear explanation including:
    - the target branch
    - the computed merge-base
    - the changed file count
    - the full changed file list

   Only if all checks pass, use the `create_pull_request` tool:
    - `base`: `${{ github.event.inputs.target_branch }}`
    - `title`: `"[${{ github.event.inputs.jira_ticket }}] Backport: <one-line summary> (master → ${{ github.event.inputs.target_branch }})"`
    - `body`: list every SHA that was cherry-picked (short form, `git rev-parse --short`), in order; for any commit where you had to resolve a conflict, describe what the conflict was and how you resolved it; include the final changed-file list and file count.
7. **Do not push directly or call the GitHub API to open the PR yourself** — pushing the branch and opening the PR is handled automatically by the `create_pull_request` safe output once you call it; you are only responsible for the local git history on the backport branch.

## Guidelines

- The backport branch must start from the target branch's tip (step 3). If you ever find yourself on `master` or on a branch whose merge-base with `origin/${{ github.event.inputs.target_branch }}` is not that branch's own tip, stop — do not attempt to fix it by merging or rebasing; call `noop` and explain what went wrong.
- Preserve commit authorship and messages via `-x` (adds a `(cherry picked from commit ...)` trailer) — do not squash or reword unless resolving a conflict requires touching the same lines.
- Be conservative when resolving conflicts: if you cannot confidently reconcile a conflict without changing behavior, leave the conflict markers in place, commit them as-is, and clearly flag in the PR body which files still need human review — do not guess silently.
- Never modify files outside the ones touched by the cherry-picked commits, except `.secrets.baseline` (regenerated after every cherry-pick, see step 5) and small adjacent edits unavoidable during conflict resolution.
- Never modify files outside the ones touched by the cherry-picked commits, except when a conflict resolution makes a small adjacent edit unavoidable.
- If the final diff against `origin/${{ github.event.inputs.target_branch }}` contains more than 100 files, do not open a PR. Call `noop` and report the full file list instead.
- If the merge-base of `HEAD` and `origin/${{ github.event.inputs.target_branch }}` is not exactly `origin/${{ github.event.inputs.target_branch }}`, do not open a PR. Call `noop` because the branch is no longer a clean backport branch.
  `master` is only where commit SHAs are validated. It must never be used as the PR base, diff base, merge base for patch generation, or fallback comparison branch at any later step.

Before calling `create_pull_request`, record the exact output of:
- `git rev-parse HEAD`
- `git rev-parse origin/${{ github.event.inputs.target_branch }}`
- `git merge-base HEAD origin/${{ github.event.inputs.target_branch }}`
- `git diff --stat origin/${{ github.event.inputs.target_branch }}...HEAD`
- `git diff --name-only origin/${{ github.event.inputs.target_branch }}...HEAD`

If these outputs do not describe a small backport branch from the target branch, call `noop` and stop.
## Safe Outputs

- **create_pull_request**: exactly one call, per the Task section above.
- **noop**: use when the checkout is shallow, the target branch doesn't exist, no commits were provided, a commit SHA fails to resolve or isn't an ancestor of master, or the backport branch was created from the wrong base — with a one-line reason.
