#!/usr/bin/env python
"""Python script to convert Hazelcast cache definitions from Java properties to XML"""
__author__="Domenico Sibilio"

import collections
import configparser
import xml.etree.ElementTree as ET
from xml.dom import minidom
from typing import List
from pathlib import Path
import argparse

ROOT_PATH = Path(__file__).parent
CONFIG_PATH = ROOT_PATH / 'ci-caches.properties'
OUTPUT_PATH = ROOT_PATH / 'alfresco-hazelcast-config.xml'
TEMPLATE_PATH = ROOT_PATH / 'alfresco-hazelcast-template.xml'
TEMPLATE_PLACEHOLDER = '<!-- CACHES DEFINITION (DO NOT REMOVE THIS PLACEHOLDER) -->'
PROPS_TO_XML = {
    # time-to-live-seconds with value = x
    'timeToLiveSeconds': 'time-to-live-seconds',
    # max-idle-seconds with value = x
    'maxIdleSeconds': 'max-idle-seconds',
    # backup-count with value = x
    'backup-count': 'backup-count',
    # read-backup-data with value = x
    'readBackupData': 'read-backup-data',
    # merge-policy with value = x
    'merge-policy': 'merge-policy',
    # eviction with eviction-policy=x and max-size-policy=PER_NODE and size=${maxItems}
    'eviction-policy': 'eviction',
    # near-cache.eviction max-size-policy=ENTRY_COUNT, eviction-policy=LRU and size = x
    'nearCache.maxSize': 'size',
    # near-cache.max-idle-seconds with value = x
    'nearCache.maxIdleSeconds': 'max-idle-seconds',
    # near-cache.time-to-live-seconds with value = x
    'nearCache.timeToLiveSeconds': 'time-to-live-seconds',
}


def get_prop(prop_key: str):
    # shortcut to get the property within the default section
    return config.get('default', prop_key)


def get_cache_name(sections: List[str]):
    # get the cache name given the full property string split by  '.'
    return '.'.join(sections[0:get_cache_name_index(sections)+1])


def get_cache_name_index(sections: List[str]):
    # returns the index where the cache name ends
    # given the full property string split by  '.'
    for i, e in enumerate(sections):
        if e.endswith('Cache'):
            return i

    return 1


def get_key(sections: List[str]):
    # get the property key name given the full property string split by  '.'
    cn_index = get_cache_name_index(sections)
    key_sections = sections[cn_index+1::]
    return '.'.join(key_sections)


def prettify(xml_string: str):
    # format and indent an xml string
    prettified_xml = minidom.parseString(xml_string).toprettyxml(indent='    ')
    return '\n'.join([line for line in prettified_xml.splitlines() if line.strip()])


# entrypoint
parser = argparse.ArgumentParser(description='A Python script to generate XML Hazelcast cache definitions starting from Java properties.')
parser.add_argument('-s', '--source', default=CONFIG_PATH, help='path to the Java properties file to convert')
args = parser.parse_args()

source_path = args.source

# add dummy section to properties
with open(source_path, 'r') as f:
    cache_props = '[default]\n' + f.read()

config = configparser.ConfigParser()
# preserve property case
config.optionxform = str
# parse config file
config.read_string(cache_props)

# group properties by cache name
props_by_cache = collections.defaultdict(list)
for item in config.items('default'):
    sections = item[0].split('.')
    if sections[0] == 'cache':
        cache_name = get_cache_name(sections)
        key = get_key(sections)
        value = item[1]
        props_by_cache[cache_name].append((key, value))

# read template file
with open(TEMPLATE_PATH, 'r') as input:
    template = input.read()

# perform template substitutions to apply the caches.properties configuration
map_configs = ''
for cache, props in props_by_cache.items():
    props = dict(props)
    if(props.get('cluster.type') == 'fully-distributed'):
        map = ET.Element('map', name=cache)
        near_cache = None
        for prop, value in props.items():
            xml_prop = PROPS_TO_XML.get(prop)
            # handle eviction configuration
            if prop == 'eviction-policy':
                ET.SubElement(map, xml_prop,
                              {'eviction-policy': value,
                               'max-size-policy': 'PER_NODE',
                               'size': props.get('maxItems') if props.get('maxItems') else '0'})
            # handle near-cache configuration
            elif prop.startswith('nearCache'):
                if near_cache is None:
                    near_cache = ET.SubElement(map, 'near-cache')
                if prop.split('.')[1] == 'maxSize':
                    ET.SubElement(near_cache, 'eviction',
                                  {'max-size-policy': 'ENTRY_COUNT',
                                   'eviction-policy': 'LRU',
                                   xml_prop: value})
                else:
                    ET.SubElement(near_cache, xml_prop).text = value
            # handle basic map configuration
            elif xml_prop:
                ET.SubElement(map, xml_prop).text = value
        ET.SubElement(map, 'per-entry-stats-enabled').text = 'true'
        map_configs += minidom.parseString(ET.tostring(map)).childNodes[0].toprettyxml(indent='    ')

template = template.replace(TEMPLATE_PLACEHOLDER, map_configs)

# produce actual Hazelcast config file
with open(OUTPUT_PATH, 'w') as output:
    output.write(prettify(template))
    print(f"Generated XML Hazelcast config: {OUTPUT_PATH}")
