#!/usr/bin/env bash
# apk-snapshot.sh <apk> <outdir>
# plan.md 부록 A.4의 스냅샷 스크립트. 단계마다 같은 스크립트로 찍어 diff로 비교한다.
set -euo pipefail

APK=$1; OUT=$2
BT=$ANDROID_HOME/build-tools/36.0.0
AN=$ANDROID_HOME/cmdline-tools/latest/bin/apkanalyzer
mkdir -p "$OUT"
unzip -l "$APK" | awk 'NR>3 {print $4}' | grep -E '^lib/'    | sort > "$OUT/libs.txt"
unzip -l "$APK" | awk 'NR>3 {print $4}' | grep -E '^assets/' | sort > "$OUT/assets.txt"
"$BT/aapt2" dump resources "$APK" | grep -E '^\s+resource 0x7f' | awk '{print $3}' | sort > "$OUT/res-names.txt"
"$BT/aapt2" dump badging "$APK" | grep -E '^(package|application-label|launchable|uses-permission|sdkVersion|targetSdkVersion)' > "$OUT/badging.txt"
# 매니페스트에는 local.properties에서 온 Kakao 키와 AdMob 앱 ID가 치환되어 들어 있다. 원문을 남기지 않고 값마다 해시 토큰으로
# 바꿔 저장한다. 같은 값은 같은 토큰이 되므로 단계 사이의 diff와 debug/release 차이는 그대로 드러난다.
"$AN" manifest print "$APK" | python3 -c '
import hashlib, re, sys
tok = lambda v: hashlib.sha256(v.encode()).hexdigest()[:8]
s = sys.stdin.read()
s = re.sub(r"kakao([0-9a-fA-F]{16,})", lambda m: "kakaoREDACTED-" + tok(m.group(1)), s)
s = re.sub(r"ca-app-pub-[0-9]+~[0-9]+", lambda m: "ca-app-pub-REDACTED-" + tok(m.group(0)), s)
sys.stdout.write(s)
' > "$OUT/manifest.xml"

{
  echo "apk: $APK"
  echo "lib entries: $(wc -l < "$OUT/libs.txt" | tr -d ' ')"
  echo "libsqliteJni.so: $(grep -c 'libsqliteJni.so' "$OUT/libs.txt" || true)"
  echo "assets/composeResources files: $(grep -c '^assets/composeResources/' "$OUT/assets.txt" || true)"
  echo "resource names: $(wc -l < "$OUT/res-names.txt" | tr -d ' ')"
  echo "manifest elements: $(python3 -c 'import sys,xml.etree.ElementTree as ET; print(sum(1 for _ in ET.parse(sys.argv[1]).getroot().iter()))' "$OUT/manifest.xml")"
} > "$OUT/summary.txt"
cat "$OUT/summary.txt"
