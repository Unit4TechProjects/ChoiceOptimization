import subprocess
import sys
from fnmatch import fnmatch

CONFIG = "DATA_FILE={}\nALLOW_GREEDY=true\n"

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

def adjust_prefs(pref_file, primary_file):
  pass #note: replace duties column with UNKNOWN again

if __name__ == "__main__":
  import os, os.path
  excel_file = sys.argv[1]

  call_command(["python2", "pref_parser.py", excel_file])
  call_command(["ant", "compile"])

  csv = [_ for _ in os.listdir(".") if fnmatch(_, "*.csv")]
  for f in csv:
    os.rename(f, "/".join(["build", f]))

  os.chdir("build/")

  for filename in csv:
    with open("scheduler.config", "w+") as f:
      f.write(CONFIG.format(filename))

    call_command(["java", "duty_scheduler.Scheduler"])

    loc, ext = path.splitext(filename)
    temp_file = "{}.temp".format(loc.replace(" ", "_"))
    adjust_prefs(filename, temp_file)
    
    raw_input("Please fill in the number of duties for each RA in the file {}".format(filename))
    
    call_command(["java", "duty_scheduler.Scheduler", "-s"])
