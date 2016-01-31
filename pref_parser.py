import pandas as pd 
import csv

LOC = "Location"

def run(excel_file):
  excel_sheet = pd.ExcelFile(excel_file)
  sheet_name = excel_sheet.sheet_names[0]
  pref_data = excel_sheet.parse(sheet_name)

  headings = [_ for _ in pref_data.head()]

  writers = {}
  files = []
  count = len(pref_data.index)

  for i in range(count):
    data_map = {headings[j]: pref_data.irow(i)[j] for j in range(len(headings))}
    user_loc = data_map[LOC]
    if user_loc not in writers:
      f = open(user_loc + ".csv", "w+")
      files.append(f)
      writers[user_loc] = csv.writer(f, lineterminator="\n")
      writers[user_loc].writerow(headings + ["Duties"])
    writers[user_loc].writerow([data_map[field] for field in headings] + ["UNKNOWN"])

  for loc_file in files:
    loc_file.close()
 
if __name__ == "__main__":
  import sys
  try:
    run(sys.argv[1])
  except Exception as e:
    print e
    sys.exit(1)
