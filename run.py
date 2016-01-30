import subprocess

parsing_errors_log = "parsing_errors.log"

if __name__ == "__main__":
  import sys
  import os, os.path
  excel_file = sys.argv[1]
  command = ["python", "parser.py", excel_file, sheet_name]
  subprocess.call(command)

  if os.path.isfile(parsing_errors_log):
    with open(parsing_errors_log) as log_file:
      print log_file.readlines()
    os.remove(parsing_errors_log)
    exit(-1)

  command = ["ant", "compile"]
