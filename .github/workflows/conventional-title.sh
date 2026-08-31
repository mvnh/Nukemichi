#!/usr/bin/env bash
# Validates a Conventional Commits header. Written as plain bash rather than pulling commitlint in,
# because that would mean a Node toolchain and a third-party action in a repository that has neither
# and pins the binaries it does download.
#
# Extending: add to TYPES only for a genuinely new kind of change; add to SCOPES whenever a new
# feature or core module appears.
set -euo pipefail

TYPES=(feat fix refactor perf test docs build ci chore style revert)
SCOPES=(
  wizard dashboard hello settings learn          # features
  vpn ssh storage ui mode security navigation di # core modules
  ci build deps release                          # infrastructure
)

# GitHub appends " (#123)" to the title when squashing, so the commit that lands is a few
# characters longer than what is checked here.
readonly MAX_LENGTH=72

title="${1-}"
errors=()
join() { local IFS='|'; echo "$*"; }

if [[ -z "$title" ]]; then
  echo "::error::The pull request title is empty."
  exit 1
fi

if [[ ! "$title" =~ ^([a-zA-Z]+)(\(([^\)]*)\))?(!)?:[[:space:]](.+)$ ]]; then
  cat <<EOF
::error::Not a Conventional Commits header: "$title"

Expected:  type(scope): subject
Examples:  fix(vpn): stop killing the :vpn process on a plain disconnect
           test(ssh): cover the connection pool's lease primitives
           feat(wizard)!: drop the legacy profile format
EOF
  exit 1
fi

type="${BASH_REMATCH[1]}"
scope="${BASH_REMATCH[3]}"
has_scope="${BASH_REMATCH[2]}"
subject="${BASH_REMATCH[5]}"

if [[ ! " ${TYPES[*]} " == *" $type "* ]]; then
  errors+=("unknown type '$type' — use one of: $(join "${TYPES[@]}")")
  case "$type" in
    ux|UX)   errors+=("  'ux' is not a type: user-visible copy or behaviour is 'feat', a layout fix is 'fix', a pure restructure is 'refactor'") ;;
    Fix|Add|Update|Remove|Merge)
             errors+=("  types are lowercase, and the subject carries the verb: '$(echo "$type" | tr '[:upper:]' '[:lower:]')' -> try 'fix:' or 'feat:'") ;;
  esac
fi

# `security` is a scope, not a type — a security fix is still fix(security) or fix(ssh).
if [[ "$type" == "security" ]]; then
  errors+=("  'security' is a scope, not a type: try 'fix(security): ...'")
fi

if [[ -n "$has_scope" ]]; then
  if [[ -z "$scope" ]]; then
    errors+=("empty scope '()' — drop the parentheses when the change has no single scope")
  elif [[ ! " ${SCOPES[*]} " == *" $scope "* ]]; then
    errors+=("unknown scope '$scope' — use one of: $(join "${SCOPES[@]}"), or drop the scope entirely")
    errors+=("  if this is a genuinely new module, add it to SCOPES in $(basename "$0")")
  fi
fi

if [[ "$subject" =~ ^[A-Z] ]]; then
  errors+=("the subject starts with a capital: '${subject}'")
fi

if [[ "$subject" == *. ]]; then
  errors+=("the subject ends with a period")
fi

if (( ${#title} > MAX_LENGTH )); then
  errors+=("the title is ${#title} characters, over the $MAX_LENGTH limit")
fi

if (( ${#errors[@]} > 0 )); then
  {
    echo "::error::\"$title\" does not follow the convention:"
    printf '  - %s\n' "${errors[@]}"
  } >&2
  exit 1
fi

echo "OK: $title"
