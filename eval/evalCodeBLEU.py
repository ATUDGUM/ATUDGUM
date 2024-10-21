import os
import json
from utils.formatter import format_code_to_single_line
from utils.calculator import calculate_codebleu

if __name__ == "__main__":
    with open(r'D:\desktop\gen\test.json', 'r', encoding='utf-8') as file:
        data = json.load(file)
    extracted_data = []
    for item in data:
        focal_src = item.get("focal_src")
        focal_tgt = item.get("focal_tgt")
        test_src = item.get("test_src")
        test_tgt = item.get("test_tgt")
        test_db = item.get("test_db", [])
        extracted_item = {
            "focal_src": focal_src,
            "focal_tgt": focal_tgt,
            "test_src": test_src,
            "test_tgt": test_tgt,
            "t_file_path": test_db[5]
        }
        extracted_data.append(extracted_item)

    base_path = 'D:/desktop/result'
    generated_file_name = 'generated_without_types_LLama—3.1_methodonly.txt'
    sum_CodeBLEU = 0.0
    test_size = 0
    no_list = [80, 249, 341, 353] # don't exist
    for i in range(1, 521):
        if i in no_list:
            continue
        folder_path = base_path + "/" + str(i)
        generated_file_path = os.path.join(folder_path, generated_file_name)
        old_t_content = extracted_data[i-1]["test_src"]
        new_t_content = extracted_data[i-1]["test_tgt"]
        old_t_content = format_code_to_single_line(old_t_content)
        new_t_content = format_code_to_single_line(new_t_content)
        if os.path.isfile(generated_file_path):
            with open(generated_file_path, 'r', encoding='utf-8') as file:
                java_code = file.read()
                generated_method = format_code_to_single_line(java_code)
                t = calculate_codebleu(new_t_content, generated_method)
                sum_CodeBLEU += t['codebleu']
                test_size += 1
                print(f"{i}: codebleu = {t['codebleu']}")
        else:
            print(f"The file does not exist")

    print(f"test_size: {test_size}, average codebleu: {sum_CodeBLEU/test_size}.")
