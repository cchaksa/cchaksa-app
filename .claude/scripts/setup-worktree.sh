#!/bin/bash
# Links the git-ignored config files (Firebase configs, local.properties, Config.xcconfig) from the
# main checkout into a new worktree, so it builds without copying secrets around.
# Registered as the repo's Orca setup hook; safe to rerun by hand from any worktree.

WORKTREE_DIR="$(git rev-parse --show-toplevel)" || exit 0
# The first entry of `git worktree list` is always the main checkout.
MAIN_DIR="$(git worktree list --porcelain | awk '/^worktree /{ print substr($0, 10); exit }')"

if [ "$WORKTREE_DIR" = "$MAIN_DIR" ]; then
  echo "setup-worktree: running in the main checkout, nothing to link."
  exit 0
fi

FILES=(
  "local.properties"
  "androidApp/google-services.json"
  "iosApp/Configuration/Config.xcconfig"
  "iosApp/iosApp/GoogleService-Info.plist"
)

for file in "${FILES[@]}"; do
  source="$MAIN_DIR/$file"
  target="$WORKTREE_DIR/$file"

  if [ ! -e "$source" ]; then
    echo "setup-worktree: skipped $file (missing in $MAIN_DIR)"
    continue
  fi
  # Keep a real file the worktree already has; only (re)place missing files and links.
  if [ -e "$target" ] && [ ! -L "$target" ]; then
    echo "setup-worktree: kept $file (a real file already exists)"
    continue
  fi

  mkdir -p "$(dirname "$target")"
  ln -sfn "$source" "$target"
  echo "setup-worktree: linked $file"
done

# Never fail the hook: a missing file should not block creating the worktree.
exit 0
