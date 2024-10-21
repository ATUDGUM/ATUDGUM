from codebleu import calc_codebleu
from codebleu.bleu import sentence_bleu

def calculate_codebleu(reference_code, generated_code):
    score = calc_codebleu([reference_code], [generated_code], lang="java", weights=(0.25, 0.25, 0.25, 0.25), tokenizer=None)
    return score

def calculate_sentencebleu(reference: str, candidate: str):
    reference_tokens = [reference.split()]
    candidate_tokens = candidate.split()
    score = sentence_bleu(reference_tokens, candidate_tokens)
    return score

