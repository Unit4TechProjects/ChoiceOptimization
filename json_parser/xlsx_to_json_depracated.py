# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import pandas as pd
import json

def get_duties(ra_name):
    # TODO consider how to import this data and actually implement this function
    return "FILL THIS IN MANUALLY"

def construct_json(path, pd_table):
    """
    From an excel file given by path, construct json input to algorithm
    """
    ras = []
    for row in pd_table.iterrows():
        row_dict = row[1].to_dict()    
        ra = {}
        ra['name'] = row_dict['Name']
        prefs = []
        for key in row_dict:
            if key != 'Name':
                # get RA preferences          
                preference = {}
                preference['duty'] = str(key)
                preference['prefVal'] = row_dict[key]
                prefs.append(preference)
                
        ra['preferences'] = prefs
        ra['duties'] = get_duties(ra['name'])
        ras.append(ra)
    
    # because row is still bound to the last value, can pull keys to get dates
    dates = []
    for key in row[1].to_dict():
        if key != "Name":
            date_dict = {}
            date = key
            date_dict['day'] = date.day
            date_dict['month'] = date.month
            date_dict['year'] = date.year
            dates.append(date_dict)
    
    loc_dicts = {}

    master_dict = {}
    master_dict['residentAssistants'] = ras
    master_dict['dates'] = dates
    
    return json.dumps(master_dict)

def run(args):
    path = args[0]
    pd_table = pd.read_excel(path, sheetname=args[1])
    print(construct_json(path, pd_table))
    

if __name__ == "__main__":
    import sys
    try:
        run(sys.argv[1:])
    except IndexError as e:
        print(e)
    except IOError as e:
        print(e)