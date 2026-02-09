"""
A script to scan through all valid mod jars in the artifacts folder,
and generate an artifact summary table for that to GitHub action step `summary`
"""
__author__ = 'Fallen_Breath(Original)' + '70CentsApple(Modified)'
# Credits to https://github.com/Fallen-Breath/fabric-mod-template

import functools
import glob
import hashlib
import json
import os


def read_prop(file_name: str, key: str) -> str:
	with open(file_name, 'r') as prop:
		return next(filter(
			lambda l: l.split('=', 1)[0].strip() == key,
			prop.readlines()
		)).split('=', 1)[1].lstrip()


def get_sha256_hash(file_path: str) -> str:
    sha256_hash = hashlib.sha256()
    with open(file_path, 'rb') as f:
        for buf in iter(functools.partial(f.read, 4096), b''):
            sha256_hash.update(buf)
    return sha256_hash.hexdigest()

def main():
    MOD_VERSION = read_prop('gradle.properties', 'mod_version')
    print(f'Current mod version: {MOD_VERSION}')
    target_subproject_env = os.environ.get('TARGET_SUBPROJECT', '')
    target_subprojects = list(filter(None, target_subproject_env.split(',') if target_subproject_env != '' else []))
    print(f'target_subprojects: {target_subprojects}')

    with open('settings.json') as f:
        settings: dict = json.load(f)

    with open(os.environ['GITHUB_STEP_SUMMARY'], 'w') as f:
        f.write('## 🍎 Build Artifacts Summary 🍎\n\n')
        f.write('| Subproject | File | Size | SHA-256 |\n')
        f.write('| --- | --- | --- | --- |\n')

        warnings = []
        for subproject in settings['versions']:
            print(f'Found: {subproject}')
            if len(target_subprojects) > 0 and subproject not in target_subprojects:
                print(f'- Skipping {subproject}')
                continue
            # file_paths = glob.glob(f'/build/libs/{MOD_VERSION}/*{subproject}*.jar')
            file_paths = glob.glob(f'gathered-artifacts/*+{subproject}*.jar')
            file_paths = list(filter(lambda fp: not any(fp.endswith(e) for e in ['-sources.jar','-dev.jar','-shadow.jar']), file_paths))
            if len(file_paths) == 0:
                file_name = '*NOT FOUND*'
                file_size = '*N/A*'
                sha256 = '*N/A*'
            else:
                file_name = f'`{os.path.basename(file_paths[0])}`'
                file_size = f'{os.path.getsize(file_paths[0])} B'
                sha256 = f'`{get_sha256_hash(file_paths[0])}`'
                if len(file_paths) > 1:
                    warnings.append(f'Found too many build files in subproject {subproject}: {", ".join(file_paths)}')

            f.write(f'| {subproject} | {file_name} | {file_size} | {sha256} |\n')

        if warnings:
            f.write('\n### Warnings\n\n')
            for warning in warnings:
                f.write(f'- {warning}\n')


if __name__ == '__main__':
    main()
