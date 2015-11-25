import pandas
import json
import traceback
from datetime import datetime
from data_validator import good_preferences

NAME = "name"
DUTY = "duty"
PREF_VAL = "prefVal"
PREFERENCES = "preferences"
DUTY_COUNT = "duties"

META_FIELDS = {
                    "LOC_FIELD": "Location", 
                    "USER_FIELD": "Username", 
                    "TIME_FIELD": "Timestamp", 
                    "NAME_FIELD": "Name"
                }

# TODO
#   add required duty counts as a field and actually implement this function
#   or incorporate some version of it in the main parser
def get_duties(ra_name):
    return "FILL THIS IN MANUALLY"

def populate(resident_assistant, row_dict):
    ra_prefs = []
    for key in row_dict:
        if key == META_FIELDS["NAME_FIELD"]:
            resident_assistant[NAME] = row_dict[META_FIELDS["NAME_FIELD"]]
        elif key not in META_FIELDS:
            preference = {}
            preference[DUTY] = str(key).split(" ")[0]
            preference[PREF_VAL]= row_dict[key]
            ra_prefs.append(preference)
    resident_assistant[PREFERENCES] = ra_prefs

def add_date(key, date_list):
    date_dict = {}
    if type(key) == datetime:
        date = key
        date_dict['day'] = date.day
        date_dict['month'] = date.month
        date_dict['year'] = date.year
    elif type(key) == str:
        date = key.split("/")
        date_dict['day'] = date[1]
        date_dict['month'] = date[0]
        date_dict['year'] = date[2]
    else:
        raise TypeError("Date field must be either a date type or a string type.")
    date_list.append(date_dict)

def construct_json(table):
    locations = {str(loc): [] for loc in table[META_FIELDS["LOC_FIELD"]]}
    for row in table.iterrows():
        row_dict = row[1].to_dict()
        resident_assistant = {}
        populate(resident_assistant, row_dict)
        pref_check = good_preferences(resident_assistant)
        if pref_check: 
            return "RA {0} has invalid preferences with error code '{1}'. Please review the spreadsheet.".format(resident_assistant[NAME], pref_check)
        resident_assistant[DUTY_COUNT] = get_duties(resident_assistant[NAME])
        locations[row_dict[META_FIELDS["LOC_FIELD"]]].append(resident_assistant)
    dates = []
    for key in row[1].to_dict():
        if key not in META_FIELDS:
            add_date(key, dates)
    return [(location, json.dumps({'residentAssistants': locs[location], 'dates': dates})) for location in locs]

def run(path, table_name, args):
    table = pandas.read_excel(path, sheetname=table_name)
    for option in args:
        opt = option.replace(" ", "").split("=")
        if len(opt) != 2:
            raise ValueError("Options must be of the form option_name=option_value")
        field = opt[0]
        value = opt[1]
        if field in META_FIELDS:
            META_FIELDS[field] = value
        else:
            raise ValueError("Unrecognized option: {0}".format(opt[0]))
    json_strings = construct_json(table)
    if isinstance(json_strings, str):
        raise ValueError(json_strings)
    else:
        for json_string in json_strings:
            with open(json_string[0] + ".json", "w") as f:
                f.write(json_string[1])

if __name__ == "__main__":
    import sys
    args_count = len(sys.argv) - 1
    if args_count < 2:
        print("Expected at least two command line args: [path] [table name] [option 1]..., but got {0} arguments.".format(args_count))
    try:
        run(sys.argv[1], sys.argv[2], sys.argv[3:])
    except(IndexError, IOError, TypeError, ValueError):
        with open("parsing_errors.log", "a") as error_log:
            error_log.write("({0})\n".format(datetime.now().strftime("%m/%d/%Y-%I:%M:%S")))
            traceback.print_exc(file=error_log)
            error_log.write("\n\n")
