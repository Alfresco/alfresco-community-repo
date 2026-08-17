---
name: Backport to release branch
emoji: "🔀"
description: Cherry-picks commits from master onto selected release branches, resolves any cherry-pick conflicts, and opens a Jira-prefixed PR per branch.
on:
  workflow_dispatch:
    inputs:
      release_25_N:
        description: "release/25.N"
        type: boolean
        default: false
      release_25_4:
        description: "release/25.4"
        type: boolean
        default: false
      release_23_7:
        description: "release/23.7"
        type: boolean
        default: false
      commits:
        description: "Commit SHAs to cherry-pick, oldest first (space/comma/newline separated, full or short)"
        type: string
        required: true
      jira_ticket:
        description: "Jira ticket ID to prefix every PR title with (e.g. ACS-1234)"
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
    max: 3
  noop:
---

# Backport to Release Branch(es)

## Current Context

- **Repository**: ${{ github.repository }}
- **Run**: ${{ github.run_id }}
- **Jira ticket**: ${{ github.event.inputs.jira_ticket }}
- **Commits to cherry-pick, in this exact order**: ${{ github.event.inputs.commits }}
- **release/25.N selected**: ${{ github.event.inputs.release_25_N }}
- **release/25.4 selected**: ${{ github.event.inputs.release_25_4 }}
- **release/23.7 selected**: ${{ github.event.inputs.release_23_7 }}

## Task

You are backporting a fixed list of commits from `master` onto one or more release branches, opening one pull request per target branch.

1. **Determine target branches.** Build the list of target branches from the three `*_selected` values above (`release/25.N`, `release/25.4`, `release/23.7` — only the ones marked `true`). If none are selected, call `noop` explaining that no target branch was chosen, and stop.

2. **Parse the commit list.** Split the commits string on whitespace, commas, and newlines to get an ordered list of SHAs (full or short). If the list is empty, call `noop` and stop.

3. **For each target branch, independently:**
   a. Make sure your working tree is clean (`git status --porcelain`), then fetch and check out the branch fresh from its remote tip: `git checkout -B backport/<branch-with-slashes-replaced-by-dashes>/${{ github.run_id }} origin/<branch>`.
   b. Cherry-pick each commit from the list, in order, with `git cherry-pick -x <sha>`.
   c. **If a cherry-pick reports a conflict**: open every file `git diff --name-only --diff-filter=U` lists, read both sides of each conflict marker, and hand-edit the file with the `edit` tool so the result preserves the intent of the original commit while fitting the current state of the target branch. Do not blindly prefer "ours" or "theirs". After resolving, `git add` the files and run `git cherry-pick --continue` (set `GIT_EDITOR=true` so it doesn't wait on an interactive commit-message prompt). If a commit is a pure no-op on this branch (already present / empty diff), skip it with `git cherry-pick --skip` and note that in the PR body.
   d. Once every commit for this branch has been applied (cleanly or with resolved conflicts), use the `create_pull_request` tool to open the PR:
      - `base`: the target branch (e.g. `release/25.4`)
      - `title`: `"[${{ github.event.inputs.jira_ticket }}] Backport: <one-line summary> (master → <branch>)"` — always start with the Jira ticket in brackets.
      - `body`: list every SHA that was cherry-picked (short form, `git rev-parse --short`), in order; for any commit where you had to resolve a conflict, describe what the conflict was and how you resolved it; for any commit skipped as a no-op, say so explicitly.
   e. Move on to the next selected branch, starting again from a clean checkout of that branch's remote tip (do not carry working-tree state between branches).

4. **Do not push directly or call the GitHub API to open PRs yourself** — pushing the branch and opening the PR is handled automatically by the `create_pull_request` safe output once you call it; you are only responsible for the local git history on each backport branch.

## Guidelines

- Preserve commit authorship and messages via `-x` (adds a `(cherry picked from commit ...)` trailer) — do not squash or reword unless resolving a conflict requires touching the same lines.
- Treat each target branch as fully independent: a conflict or resolution on one branch must not leak into another.
- Be conservative when resolving conflicts: if you cannot confidently reconcile a conflict without changing behavior, leave the conflict markers in place, commit them as-is, and clearly flag in the PR body which files still need human review — do not guess silently.
- Never modify files outside the ones touched by the cherry-picked commits.

## Safe Outputs

- **create_pull_request**: one call per selected target branch, per the Task section above.
- **noop**: use when no target branch is selected or no commits were provided, with a one-line reason.
