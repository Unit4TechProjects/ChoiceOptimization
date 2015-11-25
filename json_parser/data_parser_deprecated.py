import pandas as pd 
import json
from datetime import datetime
import traceback
from data_validator import good_preferences

LOC_FIELD = "Location"
NAME_FIELD = "Name"
TIME_FIELD = "Timestamp"
USER_FIELD = "Username"

IGNORE_FIELDS = {TIME_FIELD, USER_FIELD, LOC_FIELD, NAME_FIELD}

# TODO
#   add required duty counts as a field and actually implement this function
#   or incorporate some version of it in the main parser
def get_duties(ra_name):
    return "FILL THIS IN MANUALLY"

def construct_json(pd_table):
    locs = {}
    for loc in pd_table[LOC_FIELD]:
        locs[str(loc)] = []

    for row in pd_table.iterrows():
        row_dict = row[1].to_dict()
        ra = {}
        prefs = []
        for key in row_dict:
            if key == NAME_FIELD:
                ra['name'] = row_dict[NAME_FIELD]
            elif key not in IGNORE_FIELDS:
                preference = {}
                preference['duty'] = str(key).split(" ")[0] #implement transformation
                preference['prefVal'] = row_dict[key]
                prefs.append(preference)
        ra['preferences'] = prefs
        pref_check = good_preferences(ra)
        if pref_check:
            return "RA {0} has invalid preferences with error code '{1}'. Please review the spreadsheet.".format(ra['name'], pref_check)
        ra['duties'] = get_duties(ra['name'])
        locs[row_dict[LOC_FIELD]].append(ra)

    dates = []
    for key in row[1].to_dict():
        if key not in IGNORE_FIELDS:
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
            dates.append(date_dict)

    return [(location, json.dumps({'residentAssistants': locs[location], 'dates': dates})) for location in locs]

def run(args):
    path = args[0]
    pd_table = pd.read_excel(path, sheetname=args[1])
    for option in args[2:]:
        opt = option.replace(" ", "").split("=")
        if len(opt) != 2:
            raise ValueError("Options must be of the form option_name=option_value")
        elif opt[0] == "LOC_FIELD":
            LOC_FIELD = opt[1]
        elif opt[0] == "NAME_FIELD":
            NAME_FIELD = opt[1]
        elif opt[0] == "IGNORE_FIELDS":
            IGNORE_FIELDS = eval(opt[1])
        else:
            raise ValueError("Unrecognized option: {0}".format(opt[0]))
    json_strings = construct_json(pd_table)
    if isinstance(json_strings, str):
        raise ValueError(json_strings)
    else:
        for json_string in json_strings:
            with open(json_string[0] + ".json", "w") as f:
                f.write(json_string[1])
 
if __name__ == "__main__":
    import sys
    try:
        run(sys.argv[1:])
    except(IndexError, IOError, TypeError, ValueError):
        with open("parsing_errors.log", "a") as f:
            f.write("({0})\n".format(datetime.now().strftime("%m/%d/%Y-%I:%M:%S")))
            traceback.print_exc(file=f)
            f.write("\n\n")
