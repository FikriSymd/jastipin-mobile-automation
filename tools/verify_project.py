#!/usr/bin/env python3
"""
Memeriksa proyek Katalon Jastipin tanpa menjalankannya.

Yang diperiksa:
  1. semua berkas XML terbaca dan berformat benar
  2. setiap findTestObject menunjuk objek yang benar-benar ada
  3. setiap pemanggilan keyword menunjuk kelas dan fungsi yang ada
  4. setiap GlobalVariable yang dipakai terdaftar di profil
  5. setiap kolom berkas data yang dibaca ada di CSV-nya
  6. setiap test case di suite ada, dan punya skrip
  7. tidak ada GUID kembar
  8. kurung dan kurawal di tiap skrip seimbang
  9. setiap penanda di kode aplikasi bisa dicapai proyek ini

Dijalankan: python3 tools/verify_project.py
Keluar dengan kode 1 kalau ada temuan.
"""
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parent.parent
APP_SRC = pathlib.Path("/home/agentuser/workspace/jastipin-android/app/src/main/java")

# Penanda di dalam menu dropdown berada pada jendela Compose terpisah, di luar
# akar yang memasang testTagsAsResourceId, sehingga tidak pernah muncul sebagai
# resource-id. Isinya dipilih lewat teks, bukan lewat penanda.
VIA_TEXT = {"spProductStatus_active", "spProductStatus_inactive"}

problems = []
notes = []


def fail(msg):
    problems.append(msg)


def rel(p):
    return str(pathlib.Path(p).relative_to(ROOT))


# ------------------------------------------------------------ 1. XML
xml_files = [p for p in ROOT.rglob("*")
             if p.suffix in (".rs", ".tc", ".ts", ".dat", ".glbl", ".prj")]
for p in xml_files:
    try:
        ET.parse(p)
    except ET.ParseError as e:
        fail(f"XML rusak: {rel(p)} -> {e}")

GROOVY_SOURCES = (list((ROOT / "Scripts").rglob("*.groovy"))
                  + list((ROOT / "Keywords").rglob("*.groovy"))
                  + list((ROOT / "Test Listeners").rglob("*.groovy")))

# ------------------------------------------------------------ 2. findTestObject
rs_objects = set()
for p in (ROOT / "Object Repository").rglob("*.rs"):
    rs_objects.add(str(p.relative_to(ROOT / "Object Repository")).replace("\\", "/")[:-3])

used_objects = set()
for p in GROOVY_SOURCES:
    for m in re.finditer(r"findTestObject\('([^']+)'\)", p.read_text(encoding="utf-8")):
        used_objects.add(m.group(1))

for obj in sorted(used_objects):
    if obj not in rs_objects:
        fail(f"findTestObject menunjuk objek yang tidak ada: {obj}")
unused = sorted(rs_objects - used_objects)

# ------------------------------------------------------------ 3. keyword
KEYWORDS = ROOT / "Keywords" / "jastipin"
declared = {}
for p in sorted(KEYWORDS.glob("*.groovy")):
    declared[p.stem] = set(re.findall(r"static\s+[\w<>\[\]]+\s+(\w+)\s*\(",
                                      p.read_text(encoding="utf-8")))

used_keywords = {}
for p in GROOVY_SOURCES:
    text = p.read_text(encoding="utf-8")
    for cls, meth in re.findall(r"\b([A-Z]\w+)\.(\w+)\s*\(", text):
        if cls in declared:
            used_keywords.setdefault(cls, set()).add(meth)

for cls, meths in sorted(used_keywords.items()):
    for meth in sorted(meths):
        if meth not in declared[cls]:
            fail(f"keyword tidak ada: {cls}.{meth}")

for p in GROOVY_SOURCES:
    for m in re.finditer(r"^import jastipin\.(\w+)$", p.read_text(encoding="utf-8"), re.M):
        if not (KEYWORDS / f"{m.group(1)}.groovy").exists():
            fail(f"import kelas yang tidak ada: jastipin.{m.group(1)} di {rel(p)}")

# ------------------------------------------------------------ 3b. impor skrip
# Skrip yang memakai sebuah simbol tanpa mengimpornya baru ketahuan saat
# dijalankan, dan galatnya menyesatkan: MissingPropertyException di baris
# pertama, seolah masalahnya di logika, padahal cuma impor yang kurang.
SYMBOL_IMPORTS = {
    "Mobile": "import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile",
    "FailureHandling": "import com.kms.katalon.core.model.FailureHandling",
    "GlobalVariable": "import internal.GlobalVariable",
    "TestDataFactory": "import com.kms.katalon.core.testdata.TestDataFactory",
}
FIND_OBJECT_IMPORT = "import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject"

for p in GROOVY_SOURCES:
    text = p.read_text(encoding="utf-8")
    for sym, line in SYMBOL_IMPORTS.items():
        if re.search(rf"\b{sym}\.", text) and line not in text:
            fail(f"{rel(p)}: memakai {sym} tapi impornya tidak ada")
    if "findTestObject(" in text and FIND_OBJECT_IMPORT not in text:
        fail(f"{rel(p)}: memakai findTestObject tapi impornya tidak ada")

# Kelas keyword sendiri tidak butuh impor di dalam paketnya, jadi cek ini hanya
# berlaku untuk skrip test case.
for p in (ROOT / "Scripts").rglob("*.groovy"):
    text = p.read_text(encoding="utf-8")
    for m in re.finditer(r"\b([A-Z]\w+)\.\w+\s*\(", text):
        cls = m.group(1)
        if cls in declared and f"import jastipin.{cls}" not in text:
            fail(f"{rel(p)}: memakai {cls} tapi impornya tidak ada")

# ------------------------------------------------------------ 4. GlobalVariable
declared_gv = set(re.findall(r"<name>(G_\w+)</name>",
                             (ROOT / "Profiles" / "default.glbl").read_text(encoding="utf-8")))

used_gv = set()
for p in GROOVY_SOURCES:
    used_gv.update(re.findall(r"GlobalVariable\.(G_\w+)", p.read_text(encoding="utf-8")))

for gv in sorted(used_gv - declared_gv):
    fail(f"GlobalVariable dipakai tapi belum didaftarkan: {gv}")

# Variabel yang didaftarkan tapi tidak dibaca siapa pun hanya menambah
# permukaan yang harus dipercaya. Dicatat, bukan digagalkan.
gv_menganggur = sorted(declared_gv - used_gv)
if gv_menganggur:
    notes.append(f"variabel global terdaftar tapi tidak dipakai: {', '.join(gv_menganggur)}")

# ------------------------------------------------------------ 5. berkas data
for dat in (ROOT / "Data Files").glob("*.dat"):
    root = ET.parse(dat).getroot()
    csv_name = root.findtext("dataSourceUrl")
    csv_path = ROOT / csv_name
    if not csv_path.exists():
        fail(f"{rel(dat)} menunjuk CSV yang tidak ada: {csv_name}")
        continue
    header = [h.strip().strip('"') for h in csv_path.read_text(encoding="utf-8").splitlines()[0].split(",")]
    nama = root.findtext("name")
    for p in (ROOT / "Scripts").rglob("*.groovy"):
        text = p.read_text(encoding="utf-8")
        if f"'{nama}'" not in text:
            continue
        for col in re.findall(r"getValue\('([^']+)'\s*,", text):
            if col not in header:
                fail(f"{rel(p)} membaca kolom '{col}' yang tidak ada di {csv_name}. Kolom: {header}")
        notes.append(f"{nama}: {len(header)} kolom {header}")

# ------------------------------------------------------------ 6. suite dan skrip
tc_files = {}
for p in (ROOT / "Test Cases").rglob("*.tc"):
    tc_files[str(p.relative_to(ROOT / "Test Cases"))[:-3]] = p

scripts = {}
for d in (ROOT / "Scripts").rglob("*"):
    if d.is_dir():
        g = list(d.glob("*.groovy"))
        if g:
            scripts[str(d.relative_to(ROOT / "Scripts"))] = g[0]

for key in tc_files:
    if key not in scripts:
        fail(f"test case tanpa skrip: {key}")
for key in scripts:
    if key not in tc_files:
        fail(f"skrip tanpa test case: {key}")

for ts in (ROOT / "Test Suites").glob("*.ts"):
    root = ET.parse(ts).getroot()
    for link in root.findall("testCaseLink"):
        tcid = link.findtext("testCaseId")
        if tcid is None or not tcid.startswith("Test Cases/"):
            fail(f"{rel(ts)}: testCaseId tidak wajar: {tcid}")
            continue
        if tcid[len("Test Cases/"):] not in tc_files:
            fail(f"{rel(ts)} menunjuk test case yang tidak ada: {tcid}")

# ------------------------------------------------------------ 7. GUID kembar
seen = {}
for p in xml_files:
    text = p.read_text(encoding="utf-8")
    for tag in ("testCaseGuid", "testSuiteGuid", "elementGuidId", "dataFileGUID",
                "webElementGuid", "UUID"):
        for m in re.finditer(rf"<{tag}>([0-9a-f-]{{36}})</{tag}>", text):
            g = m.group(1)
            if g in seen:
                fail(f"GUID kembar {g}: {rel(p)} dan {seen[g]}")
            seen[g] = rel(p)

# ------------------------------------------------------------ 8. keseimbangan kurung
for p in GROOVY_SOURCES:
    bersih = re.sub(r"//[^\n]*", "", p.read_text(encoding="utf-8"))
    bersih = re.sub(r"/\*.*?\*/", "", bersih, flags=re.S)
    bersih = re.sub(r"'[^'\n]*'", "''", bersih)
    bersih = re.sub(r'"[^"\n]*"', '""', bersih)
    for buka, tutup, nama in (("{", "}", "kurawal"), ("(", ")", "kurung"), ("[", "]", "siku")):
        if bersih.count(buka) != bersih.count(tutup):
            fail(f"{rel(p)}: {nama} tidak seimbang "
                 f"({bersih.count(buka)} buka, {bersih.count(tutup)} tutup)")

# ------------------------------------------------------------ 9. penanda aplikasi
if APP_SRC.exists():
    tags = set()
    for p in APP_SRC.rglob("*.kt"):
        for m in re.finditer(r'testTag\("([^"]+)"\)', p.read_text(encoding="utf-8")):
            tags.add(m.group(1))
    fixed = {t for t in tags if "$" not in t and t not in ("testTag", "tag")}

    static_tags = set()
    for p in (ROOT / "Object Repository").rglob("*.rs"):
        v = ET.parse(p).getroot().find(".//value")
        if v is not None and v.text:
            static_tags.add(v.text)

    dynamic = (KEYWORDS / "Jastipin.groovy").read_text(encoding="utf-8")
    dynamic_bases = set(re.findall(r"resourceId\('([A-Za-z]+_)'", dynamic))

    tak_terjangkau = [t for t in sorted(fixed)
                      if t not in static_tags and t not in VIA_TEXT
                      and not any(t.startswith(b) for b in dynamic_bases)]
    notes.append(f"penanda aplikasi: {len(fixed)} tetap, {len(static_tags)} punya objek statis, "
                 f"{len(dynamic_bases)} pola dinamis, {len(VIA_TEXT)} dijangkau lewat teks")
    for t in tak_terjangkau:
        fail(f"penanda aplikasi tidak terjangkau proyek: {t}")

# ------------------------------------------------------------ 10. larangan scrollToText
# Mobile.scrollToText tidak bisa diandalkan pada daftar Compose: elemen di bawah
# belum disusun, jadi belum ada di pohon elemen dan tidak bisa dicari lewat teks.
# Pengguliran harus lewat geser layar berulang.
for p in GROOVY_SOURCES:
    for m in re.finditer(r"Mobile\.scrollToText\s*\(", p.read_text(encoding="utf-8")):
        fail(f"{rel(p)}: memakai Mobile.scrollToText, yang tidak andal pada Compose. "
             f"Pakai MobileActions.scrollToOrFail.")

# --------------------------------------------------- 11. jarak dan kecepatan geser
# mobile: swipeGesture memakai kecepatan bawaan 5000 x kerapatan layar. Geseran
# menjadi lemparan yang meluncur jauh melewati sasaran, sehingga elemen di tengah
# daftar terlewat di antara dua pemeriksaan. Kecepatan harus disebut eksplisit.
ma = ROOT / "Keywords/jastipin/MobileActions.groovy"
if ma.exists():
    isi = ma.read_text(encoding="utf-8")
    if "swipeGesture" in isi and "speed" not in isi:
        fail("geserSekali memanggil swipeGesture tanpa menyebut kecepatan; "
             "bawaan 5000 x kerapatan layar membuat geseran meluncur melewati sasaran")
    if "swipeGesture" in isi and "percent" not in isi:
        fail("geserSekali memanggil swipeGesture tanpa menyebut jarak (percent); "
             "jarak bawaan membuat satu geseran melompati lebih dari satu kartu")

# Daftar bergrid: satu geseran tidak boleh lebih panjang dari tinggi satu kartu.
# Kartu katalog terukur 693 px, sedangkan langkah 0,9 menghasilkan 1.264 px, hampir
# dua kali tinggi kartu. Satu geseran bisa melompati satu baris penuh, dan baris yang
# terlewat berbeda tiap eksekusi karena posisi awal gulir bergeser sedikit.
cs = ROOT / "Keywords/jastipin/CatalogScreen.groovy"
if cs.exists():
    rapat = re.sub(r"\s+", " ", cs.read_text(encoding="utf-8"))
    m = re.search(r"scrollToOrFail\(\s*Jastipin\.productCard\([^)]*\)\s*,\s*\d+\s*,\s*[^,]+,\s*([0-9.]+)\s*\)", rapat)
    if not m:
        fail("CatalogScreen.scrollToProduct tidak menyebut jarak geser; daftar kartu "
             "butuh langkah di bawah 0,5 supaya tidak ada kartu yang terlewat")
    elif float(m.group(1)) > 0.5:
        fail(f"jarak geser katalog {m.group(1)} terlalu panjang: satu geseran "
             f"melompati lebih dari satu kartu, sehingga kartu di baris tengah bisa "
             f"terlewat. Pakai 0,5 atau kurang.")

# ------------------------------------------------------------ laporan
print("=== Ringkasan ===")
print(f"berkas XML diperiksa          : {len(xml_files)}")
print(f"objek di Object Repository    : {len(rs_objects)}")
print(f"objek yang dipakai skrip      : {len(used_objects)}")
print(f"kelas keyword                 : {len(declared)} -> {', '.join(sorted(declared))}")
print(f"test case                     : {len(tc_files)}")
print(f"test suite                    : {len(list((ROOT / 'Test Suites').glob('*.ts')))}")
print(f"variabel global               : {len(declared_gv)}")
print(f"GUID unik                     : {len(seen)}")
for n in notes:
    print(f"catatan: {n}")
if unused:
    print()
    print(f"objek yang belum dipakai skrip ({len(unused)}), tersedia untuk kasus baru:")
    for o in unused:
        print(f"  - {o}")

print()
if problems:
    print(f"TEMUAN ({len(problems)}):")
    for i, m in enumerate(problems, 1):
        print(f"  {i}. {m}")
    sys.exit(1)

print("TIDAK ADA TEMUAN. Struktur proyek konsisten.")
