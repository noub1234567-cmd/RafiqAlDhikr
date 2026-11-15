"""Fail if the migration SQL in AppDatabase.kt differs from what Room generated for the same tables."""
import json, re, sys, glob

schemas = sorted(glob.glob("RafiqAlDhikr/app/schemas/**/2.json", recursive=True))
if not schemas:
    sys.exit("no exported schema 2.json found")
db = json.load(open(schemas[0]))["database"]
src = open("RafiqAlDhikr/app/src/main/java/com/rafiqaldhikr/app/data/local/db/AppDatabase.kt", encoding="utf-8").read()

def kotlin_const(name):
    m = re.search(r"const val " + name + r" =\s*((?:\"[^\"\n]*\"\s*\+?\s*)+)", src)
    if not m:
        sys.exit("constant not found: " + name)
    return "".join(re.findall(r"\"([^\"\n]*)\"", m.group(1)))

norm = lambda s: re.sub(r"\s+", "", s)
ent = next(e for e in db["entities"] if e["tableName"] == "prayer_logs")
want_table = norm(ent["createSql"].replace("${TABLE_NAME}", "prayer_logs"))
want_index = norm(ent["indices"][0]["createSql"].replace("${TABLE_NAME}", "prayer_logs"))
got_table = norm(kotlin_const("PRAYER_LOGS_CREATE_SQL"))
got_index = norm(kotlin_const("PRAYER_LOGS_INDEX_SQL"))
ok = True
if want_table != got_table:
    ok = False; print("TABLE MISMATCH\n room:", want_table, "\n code:", got_table)
if want_index != got_index:
    ok = False; print("INDEX MISMATCH\n room:", want_index, "\n code:", got_index)
if not ok:
    sys.exit(1)
print("migration SQL matches Room's generated schema")
