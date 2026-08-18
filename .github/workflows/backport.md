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
---

# Backport to Release Branch

## Current Context

- **Repository**: ${{ github.repository }}
- **Run**: ${{ github.run_id }}
- **Jira ticket**: ${{ github.event.inputs.jira_ticket }}
- **Target branch**: ${{ github.event.inputs.target_branch }}
- **Commits to cherry-pick, in this exact order**: ${{ github.event.inputs.commits }}

## Task

You are backporting a fixed list of commits from `master` onto the target branch above and opening a single pull request.

1. **Validate the target branch.** Confirm `origin/${{ github.event.inputs.target_branch }}` exists (`git rev-parse --verify`). If it does not exist, call `noop` explaining the branch was not found, and stop.

2. **Parse the commit list.** Split the commits string on whitespace, commas, and newlines to get an ordered list of SHAs (full or short). If the list is empty, call `noop` and stop.

3. **Check out the target branch fresh.** Make sure your working tree is clean (`git status --porcelain`), then check out the branch fresh from its remote tip: `git checkout -B backport/<target-branch-with-slashes-replaced-by-dashes>/${{ github.run_id }} origin/${{ github.event.inputs.target_branch }}`.

4. **Cherry-pick each commit**, in order, with `git cherry-pick -x <sha>`.

5. **If a cherry-pick reports a conflict**: open every file `git diff --name-only --diff-filter=U` lists, read both sides of each conflict marker, and hand-edit the file with the `edit` tool so the result preserves the intent of the original commit while fitting the current state of the target branch. Do not blindly prefer "ours" or "theirs". After resolving, `git add` the files and run `git cherry-pick --continue` (set `GIT_EDITOR=true` so it doesn't wait on an interactive commit-message prompt). If a commit is a pure no-op on this branch (already present / empty diff), skip it with `git cherry-pick --skip` and note that in the PR body.

6. **Once every commit has been applied** (cleanly or with resolved conflicts), use the `create_pull_request` tool to open the PR:
   - `base`: `${{ github.event.inputs.target_branch }}`
   - `title`: `"[${{ github.event.inputs.jira_ticket }}] Backport: <one-line summary> (master → ${{ github.event.inputs.target_branch }})"` — always start with the Jira ticket in brackets.
   - `body`: list every SHA that was cherry-picked (short form, `git rev-parse --short`), in order; for any commit where you had to resolve a conflict, describe what the conflict was and how you resolved it; for any commit skipped as a no-op, say so explicitly.

7. **Do not push directly or call the GitHub API to open the PR yourself** — pushing the branch and opening the PR is handled automatically by the `create_pull_request` safe output once you call it; you are only responsible for the local git history on the backport branch.

## Guidelines

- Preserve commit authorship and messages via `-x` (adds a `(cherry picked from commit ...)` trailer) — do not squash or reword unless resolving a conflict requires touching the same lines.
- Be conservative when resolving conflicts: if you cannot confidently reconcile a conflict without changing behavior, leave the conflict markers in place, commit them as-is, and clearly flag in the PR body which files still need human review — do not guess silently.
- Never modify files outside the ones touched by the cherry-picked commits.

## Safe Outputs

- **create_pull_request**: exactly one call, per the Task section above.
- **noop**: use when the target branch doesn't exist or no commits were provided, with a one-line reason.
