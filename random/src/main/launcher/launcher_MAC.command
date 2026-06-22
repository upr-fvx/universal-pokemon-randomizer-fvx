cd "$( dirname "$0" )"
chmod +x java/bin/java
java/bin/java -Xmx4608M -jar UPR-FVX.jar please-use-the-launcher
echo "Press Enter to exit..."
read -s