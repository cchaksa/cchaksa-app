# manifest_diff.py <before.xml> <after.xml>: 순서를 무시하고 요소·속성 집합을 비교한다
# plan.md 부록 A.4. 판정 기준은 양쪽 "only in" 목록이 모두 비는 것이다.
import sys, xml.etree.ElementTree as ET
A = '{http://schemas.android.com/apk/res/android}'
def records(path):
    out = set()
    def walk(e, parent):
        attrs = tuple(sorted((k.replace(A, 'android:'), v) for k, v in e.attrib.items()))
        out.add((parent, e.tag, attrs))
        name = e.attrib.get(A + 'name', '')
        for c in e:
            walk(c, f"{parent}/{e.tag}" + (f"[{name}]" if name else ""))
    walk(ET.parse(path).getroot(), '')
    return out
a, b = records(sys.argv[1]), records(sys.argv[2])
print("only in before:", *sorted(a - b), sep="\n  ")
print("only in after:", *sorted(b - a), sep="\n  ")
