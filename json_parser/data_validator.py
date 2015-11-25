
INCONSISTENT = "INCONSISTENT"
GREEDY = "GREEDY"

def good_preferences(ra):
    prefs = ra[preferences]
    blacked_out = len([pref for pref in prefs if int(pref['prefVal']) == 0])
    doable_count = len(prefs) - blacked_out + 1
    if doable_count < len(prefs) // 2:
        return GREEDY
    expected = {i for i in range(1, doable_count)}
    for pref in prefs:
        try:
            expected.remove(int(pref['prefVal']))
        except Exception:
            return INCONSISTENT
    if expected:
        return INCONSISTENT
        