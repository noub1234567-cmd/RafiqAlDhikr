"""Fail if the migration SQL in AppDatabase.kt differs from what Room generated for the same tables."""
import json, re, sys, glob

schemas = sorted(glob.glob("RafiqAlDhikr/app/schemas/**/3.json", recursive=True))
if not schemas:
    sys.exit("no exported schema 3.json found")
db = json.load(open(schemas[0]))["database"]
src = open("RafiqAlDhikr/app/src/main/java/com/rafiqaldhikr/app/data/local/db/AppDatabase.kt", encoding="utf-8").read()

def kotlin_const(name):
    m = re.search(r"const val " + name + r" =\s*((?:\"[^\"\n]*\"\s*\+?\s*)+)", src)
    if not m:
        sys.exit("constant not found: " + name)
    return "".join(re.findall(r"\"([^\"\n]*)\"", m.group(1)))

norm = lambda s: re.sub(r"\s+", "", s)
ok = True
for table, create_const, index_const in (
    ("prayer_logs", "PRAYER_LOGS_CREATE_SQL", "PRAYER_LOGS_INDEX_SQL"),
    ("khatmas", "KHATMAS_CREATE_SQL", "KHATMAS_INDEX_SQL"),
):
    ent = next(e for e in db["entities"] if e["tableName"] == table)
    want_table = norm(ent["createSql"].replace("${TABLE_NAME}", table))
    want_index = norm(ent["indices"][0]["createSql"].replace("${TABLE_NAME}", table))
    if want_table != norm(kotlin_const(create_const)):
        ok = False; print(table, "TABLE MISMATCH\n room:", want_table, "\n code:", norm(kotlin_const(create_const)))
    if want_index != norm(kotlin_const(index_const)):
        ok = False; print(table, "INDEX MISMATCH\n room:", want_index, "\n code:", norm(kotlin_const(index_const)))
if not ok:
    sys.exit(1)
print("migration SQL matches Room's generated schema")
