# pipes all logging to also go to console_output.log
exec 1> >(tee -a console_output.log >&1)
exec 2> >(tee -a console_output.log >&2)
printf '%s\n' "===== Launching the Universal Pokémon Randomizer FVX at $(date) =====" >&1

DIR=$(dirname "$0")
cd "$DIR" || {
  printf '%s\n' "Error: cannot change the working directory to that of the launcher: $DIR" >&2
  exit 1
}

JAVA="./java/bin/java"

# Detect embedded Java missing
if [ ! -f "$JAVA" ]; then
  printf '%s\n' "Error: embedded Java runtime not found at: $JAVA" >&2
  exit 1
fi

# Detect embedded Java / system architecture mismatch
ARCH=$(file -b "$JAVA")
if printf '%s' "$ARCH" | grep -q "x86-64"; then
    JAVA_ARCH="x86"
elif printf '%s' "$ARCH" | grep -q "aarch64\|arm64"; then
    JAVA_ARCH="ARM"
else
    JAVA_ARCH="unknown"
fi

# Detect system architecture
SYS_ARCH=$(uname -m)
case "$SYS_ARCH" in
    x86_64) SYS_ARCH="x86" ;;
    aarch64|arm64) SYS_ARCH="ARM" ;;
esac

if [ "$JAVA_ARCH" != "$SYS_ARCH" ]; then
    printf '%s\n' "Error: embedded Java architecture ($JAVA_ARCH) does not match system architecture ($SYS_ARCH)." >&2
    printf '%s\n' "Hint: there are UPR FVX downloads for both x86 and ARM. Make sure you downloaded the right version." >&2
    exit 1
fi

# Detect Mac App Translocation
if printf '%s\n' "$DIR" | grep -q "/AppTranslocation/"; then
    printf '%s\n' "Error: Mac App Translocation is active." >&2
    printf '%s\n' "Hint: Move UPR FVX out of the Downloads folder." >&2
    exit 1
fi

# Mac quarantine fix
xattr -dr com.apple.quarantine "$DIR" 2>/dev/null
# Set executable flag
chmod +x "$JAVA"

# Detect it still not being executable, for mystery reasons
if [ ! -x "$JAVA" ]; then
  printf '%s\n' "Error: embedded Java runtime is not executable: $JAVA" >&2
  exit 1
fi

exec "$JAVA" -Xmx4608M -jar UPR-FVX.jar please-use-the-launcher
