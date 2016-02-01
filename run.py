import subprocess
import sys
import csv
import re
import os, os.path
from fnmatch import fnmatch

CONFIG = "DATA_FILE={}\nALLOW_GREEDY=true\n"
DATE_CHECK = re.compile("[0-9]{1,2}([/\\-\\.])[0-9]{1,2}\\1[0-9]{4}")

def print_command_err(command, ret_code):
  make_arg = lambda x: x.replace(" ", "\\ ")
  command = [make_arg(_) for _ in command]
  cmd_str = " ".join(command)
  print "ExecutionError: command {%s} returned error code %d when executing" % (cmd_str, ret_code)
  sys.exit(ret_code)

def call_command(command):
  ret_code = subprocess.call(command)
  if ret_code:
    print_command_err(command, ret_code)

def write_config_file(filename):
  with open("scheduler.config", "w+") as ff:
    ff.write(CONFIG.format(filename))

def get_temp_file(original_file):
  loc, ext = os.path.splitext(original_file)
  return "{}.temp".format(loc.replace(" ", "_"))

def merge_prefs(pref_files):
  """ Merges the primary preference files into one map. NOTE: Assumes csv files are identically structured """
  ra_map = {}
  name_index = 0
  for ff in pref_files:
    with open(ff, "r") as rf:
      csv_reader = csv.reader(rf, lineterminator="\n")
      header = None
      for row in csv_reader:
        if not header:
          header = {row[i]: i for i in xrange(len(row))}
          name_index = header["Name"]
        else:
          ra_map[row[name_index]] = {i: row[header[i]] 
                                      for i in header 
                                        if (DATE_CHECK.match(i) or i == "Duties")}
  return ra_map, [_ for _ in header.keys() 
                      if (DATE_CHECK.match(_) or _ == "Duties" or _ == "Name")]

def update_secondary_prefs(ra_map, primary_files):
  for ff in primary_files:
    with open(ff, "r") as rf:
      csv_reader = csv.reader(rf, lineterminator="\n")
      for row in csv_reader:
        ra = ra_map[row[0]]
        ra["Duties"] = "UNKNOWN"
        for duty in row[1:]:
          ra[duty] = "0"
  return ra_map

def write_new_prefs(new_prefs, headings):
  def key_function(x):
    if x == "Name":
      return 0
    elif x == "Duties":
      return 32
    elif DATE_CHECK.match(x):
      return int(x.split("/")[1])

  with open("secondary.csv", "w+") as wf:
    csv_writer = csv.writer(wf, lineterminator="\n")
    headings = sorted(headings, key=key_function)
    csv_writer.writerow(headings)
    for name in new_prefs.keys():
      prefs = new_prefs[name]
      csv_writer.writerow([name] + [prefs[heading] for heading in headings if heading != "Name"]) 

def create_secondary_prefs(pref_files, primary_data_files):
  primary_ra_map, headers = merge_prefs(pref_files)
  secondary_ra_map = update_secondary_prefs(primary_ra_map, primary_data_files)
  write_new_prefs(secondary_ra_map, headers)

if __name__ == "__main__":
  excel_file = sys.argv[1]

  call_command(["python2", "pref_parser.py", excel_file])
  call_command(["ant", "compile"])

  csv_files = [_ for _ in os.listdir(".") if fnmatch(_, "*.csv")]
  for ff in csv_files:
    os.rename(ff, "/".join(["build", ff]))

  os.chdir("build/")

  for filename in csv_files:
    write_config_file(filename)
    raw_input("Please fill in the number of primary duties for each RA in the file %s.\nPress Enter when finished" % (filename))
    call_command(["java", "duty_scheduler.Scheduler"])
  
  create_secondary_prefs(csv_files, [get_temp_file(filename) for filename in csv_files]) 
  raw_input("Please fill in the number of secondary duties for each RA in the file secondary.csv.\nPress Enter when finished")
  
  write_config_file("secondary.csv")
  call_command(["java", "duty_scheduler.Scheduler", "-s"])
