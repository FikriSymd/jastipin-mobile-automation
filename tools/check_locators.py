#!/usr/bin/env python3
"""
Check every Object Repository locator against a live UI hierarchy dump.

The dump comes from `adb shell uiautomator dump` (see tools/dump-hierarchy.sh).

Usage:
    python3 tools/check_locators.py ui-dump.xml

Output: one line per locator, FOUND or MISSING, plus a short suggestion when a
similar attribute value exists in the dump. Read-only, it never edits the project.
"""
import os
import re
import sys
import xml.etree.ElementTree as ET

REPO_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'Object Repository')


def load_nodes(dump_path):
    tree = ET.parse(dump_path)
    nodes = []
    for node in tree.iter('node'):
        nodes.append(dict(node.attrib))
    return nodes


def load_locators():
    """Returns [(relative_path, [(prop_name, value, match_condition), ...]), ...]."""
    out = []
    for root, _, files in os.walk(REPO_DIR):
        for name in sorted(files):
            if not name.endswith('.rs'):
                continue
            path = os.path.join(root, name)
            try:
                tree = ET.parse(path)
            except ET.ParseError as exc:
                print('WARN  cannot parse %s: %s' % (path, exc))
                continue
            props = []
            for prop in tree.iter('webElementProperties'):
                selected = (prop.findtext('isSelected') or '').strip().lower()
                if selected != 'true':
                    continue
                props.append((
                    (prop.findtext('name') or '').strip(),
                    (prop.findtext('value') or ''),
                    (prop.findtext('matchCondition') or 'equals').strip(),
                ))
            rel = os.path.relpath(path, REPO_DIR).replace(os.sep, '/')
            out.append((rel[:-3], props))
    return out


def matches(node_value, expected, condition):
    if node_value is None:
        return False
    if condition == 'equals':
        return node_value == expected
    if condition == 'contains':
        return expected in node_value
    if condition == 'startsWith':
        return node_value.startswith(expected)
    if condition == 'endsWith':
        return node_value.endswith(expected)
    if condition in ('regex', 'matches'):
        try:
            return re.search(expected, node_value) is not None
        except re.error:
            return False
    return node_value == expected


def locator_hits(nodes, props):
    hits = []
    for node in nodes:
        if all(matches(node.get(p), v, c) for p, v, c in props):
            hits.append(node)
    return hits


def suggest(nodes, props):
    """Find dump values that share a substring with the first property value."""
    if not props:
        return None
    prop_name, expected, _ = props[0]
    needle = expected.split(':id/')[-1] if ':id/' in expected else expected
    if len(needle) < 4:
        return None
    close = set()
    for node in nodes:
        value = node.get(prop_name)
        if not value:
            continue
        if needle.lower() in value.lower() or value.lower() in needle.lower():
            close.add(value)
    if close:
        return sorted(close)[:3]
    return None


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        return 2

    dump_path = sys.argv[1]
    if not os.path.isfile(dump_path):
        print('ERROR: dump file not found: %s' % dump_path)
        return 1
    if not os.path.isdir(REPO_DIR):
        print('ERROR: Object Repository not found at %s' % REPO_DIR)
        return 1

    nodes = load_nodes(dump_path)
    locators = load_locators()
    print('dump nodes      : %d' % len(nodes))
    print('locators checked: %d' % len(locators))
    print('')

    found = 0
    missing = []

    for rel, props in locators:
        if not props:
            print('SKIP  %-42s (no selected property)' % rel)
            continue
        hits = locator_hits(nodes, props)
        if hits:
            found += 1
            desc = props[0][1]
            print('FOUND %-42s %s' % (rel, desc))
        else:
            missing.append(rel)
            print('MISS  %-42s %s' % (rel, props[0][1]))
            hint = suggest(nodes, props)
            if hint:
                for h in hint:
                    print('        mirip: %s' % h)

    print('')
    print('hasil: %d cocok, %d meleset' % (found, len(missing)))
    if missing:
        print('')
        print('Locator yang meleset:')
        for rel in missing:
            print('  - %s' % rel)
        print('')
        print('Catatan: locator yang menunjuk elemen di dalam daftar (item drawer, baris hasil')
        print('pencarian) memang hanya cocok kalau layarnya sedang terbuka saat dump dibuat.')
        print('Buka layarnya dulu di emulator, baru jalankan dump dan cek ulang.')

    return 0


if __name__ == '__main__':
    sys.exit(main())
