import os

from dotenv import dotenv_values, load_dotenv
from openai import OpenAI
import json
import random
import re
import subprocess
import requests

load_dotenv()
config = dotenv_values(".env")
client = OpenAI(api_key=os.environ.get(config['OPENAI_API_KEY']))
file_path = 'books-core/src/main/java/com/sismics/books/core/dao/jpa/UserDao.java'
repo_url = 'https://github.com/serc-courses/se-project-1--_18'
repo_owner = 'serc-courses'
repo_name = 'se-project-1--_18'

def create_git_branch(branch_name):
    try:
        # Run the Git command to create a new branch
        subprocess.run(["git", "branch", branch_name])
        print(f"Branch '{branch_name}' created successfully.")
        subprocess.run(["git", "checkout", branch_name])

    except Exception as e:
        print(f"Error occurred while creating branch '{branch_name}': {e}")

def push_git_branch(branch_name):
    try:
        subprocess.run(["git", "push", "--set-upstream", "origin", branch_name])
        print(f"Branch '{branch_name}' pushed to remote origin successfully.")
    except Exception as e:
        print(f"Error occurred while creating branch '{branch_name}': {e}")

def commit_files(file_paths, commit_message):
    try:
        print('wtfffff...................', file_paths)
        # Add the specified files to the staging area
        subprocess.run(["git", "add"] + file_paths)

        subprocess.run(["git", "commit", "-m", commit_message])
        
        print("Files committed successfully.")
    except Exception as e:
        print(f"Error occurred while committing files: {e}")

def retrieve_code_blocks(input_string):
    pattern = r"```java(.*?)```"
    match = re.search(pattern, input_string, re.DOTALL)
    if match:
        return match.group(1)  # Return the text captured by the first group
    else:
        return None

def retrieve_file_name(input_string):
    pattern = r"\[(.*?)\]"
    match = re.search(pattern, input_string)
    if match:
        return match.group(1).strip()  # Return the text captured by the first group
    else:
        return None  # Return None if no match is found

def detect_design_smells(file_content):
    messages = [
        {
            "role": "user",
            "content": f"Identify design smells (Dont give trivial smells) in the following code:\n\n{file_content}\n\nGive in the format <id>;<design smell>;<description>;<suggested refactoring>;\\n",
        }
    ]
    response = client.chat.completions.create(
        messages=messages,
        model="gpt-3.5-turbo",
    )
    print(response.choices[0].message.content)
    detected_smells = [ x.split(';') for x in response.choices[0].message.content.strip().split('\n')]
    detected_smells = [{'smell name': x[1], 'smell description': x[2], 'suggested refactoring': x[3]} for x in detected_smells]
    return detected_smells

def refactor_code(file_content, design_smells):
    
    selected_design_smell = random.choice(range(design_smells.__len__()))
    selected_design_smell = design_smells[selected_design_smell]

    prompt = f"Refactor the below code with respect to the specified design pattern (dont give trivial refactoring)\nOriginal Code:\n{file_content}\n\nIdentified Design Smell:\n\n{selected_design_smell}\n\nGive Output in the format for all the new files or existing file:\n[<file name>]\n```java\n<code that goes into the file>\n```|\n[<file name>]\n```java\n<code that goes into the file>\n```|...etc..."
    
    messages = [
        {
            "role" : "user",
            "content": prompt
        }
    ]

    response = client.chat.completions.create(
        messages=messages,
        model="gpt-3.5-turbo",
    )
    print('------------------------------------------')
    print('Refactoring')
    print('------------------------------------------')
    print(response.choices[0].message.content)
    refactored_code = response.choices[0].message.content
    refactored_code = refactored_code.strip().split('|')
    print(refactored_code)
    refactored_code = [{"file_name": retrieve_file_name(x), 'file_content': retrieve_code_blocks(x)} for x in refactored_code]
    for i in refactored_code:
        if(i['file_name'] == None or i['file_content'] == None):
            continue

        base_path = os.path.dirname(file_path)
        new_file_path = os.path.join(base_path, i['file_name'])
        print(new_file_path)
        with open(new_file_path, 'w+') as opened_file:
            opened_file.write(i['file_content'])

    return refactored_code, selected_design_smell

# Step 3: Pull Request Generation
def create_pull_request(refactored_code, repo_owner, repo_name, refactored_smell):
    branch_name = refactored_smell["smell name"].replace(' ', '-') + "-" + str(random.choice(range(1000)))
    create_git_branch(branch_name)
    files = []
    for i in refactored_code:
        if i["file_name"] != None:
            files.append(os.path.join(os.path.dirname(file_path), i["file_name"]))

    commit_files(files, refactored_smell["smell name"] + " Solved")
    push_git_branch(branch_name)

    # Implement GitHub API calls to create a pull request
    url = f'https://api.github.com/repos/{repo_owner}/{repo_name}/pulls'
    headers = {
        'Accept': 'application/vnd.github+json',
        'Authorization': f'Bearer {github_token}',
        'X-GitHub-Api-Version': '2022-11-28'
    }

    # Define the pull request details
    pull_request_data = {
        'title': f'Automated Refactoring: {refactored_smell["smell name"]}',
        'body': f'# Smell description\n{refactored_smell["smell description"]}\n\n# Refactoring: \n{refactored_smell["suggested refactoring"]}',
        'head': branch_name,
        'base': 'master',
    }

    print(url, headers, pull_request_data)
    # Create the pull request
    response = requests.post(url, headers=headers, json=pull_request_data)

    if response.status_code == 201:
        print('Pull request created successfully!')
    else:
        print(f'Error creating pull request. Status code: {response.status_code}, Message: {response.text}')

# Read the original code from the file

with open(file_path, 'r') as file:
    original_code = file.read()

detected_smells = detect_design_smells(original_code)
json_data = json.dumps(detected_smells, indent=4)

with open('design_smells.json', 'w+') as f:
    f.write(json_data)

with open('./docs/Automated-refactoring/report.md', 'a+') as report_file:
    for smell in detected_smells:
        report_file.write(f"## {smell['smell name']}\n\n")
        report_file.write(f"**Description:** {smell['smell description']}\n\n")
        report_file.write(f"**Suggested Refactoring:** {smell['suggested refactoring']}\n\n")

print(detected_smells)

refactored_code, refactored_smell = refactor_code(original_code, detected_smells)


# Step 1: Detect design smells
#print(detected_smells)
# # Step 2: Refactor code
# refactored_code = refactor_code(original_code)

# refactored_code = [
#     {
#         "file_name": "TagDao.java"
#     }
# ]
# refactored_smell = {
#     "smell name": "Magic String Smell",
#     "smell description": "String literals are used directly in queries, making the code less maintainable",
#     "suggested refactoring": "Use constants or enums to define the query strings and parameter names."
# }
create_pull_request(refactored_code, repo_owner, repo_name, refactored_smell)
# # Step 3: Create pull request
# create_pull_request(repo_owner, repo_name, file_path, original_code, refactored_code, detected_smells)