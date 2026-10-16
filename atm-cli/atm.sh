#!/usr/bin/env sh
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "$DIR/target/atm.jar" "$@"
