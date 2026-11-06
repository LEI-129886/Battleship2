#!/usr/bin/env python3
"""Fail if any Java method/constructor declares more than N parameters."""
import argparse
import os
import re
import subprocess
import sys

KEYWORDS = {
    "return", "new", "throw", "else", "case", "yield", "assert", "if", "for",
    "while", "switch", "catch", "try", "synchronized", "do", "instanceof",
    "super", "this", "default", "import", "package",
}
IDENT = re.compile(r"[A-Za-z_$][\w$]*")
SKIP_DIRS = {".git", "build", "target", "out", "node_modules", ".gradle", ".idea"}


def blank_comments_and_strings(src: str) -> str:
    """Replace comments/string/char literals with spaces, keeping newlines."""
    out, i, n = [], 0, len(src)
    while i < n:
        c, nxt = src[i], src[i + 1] if i + 1 < n else ""
        if c == "/" and nxt == "/":
            j = src.find("\n", i)
            j = n if j == -1 else j
            out.append(" " * (j - i)); i = j
        elif c == "/" and nxt == "*":
            j = src.find("*/", i + 2)
            j = n if j == -1 else j + 2
            out.append(re.sub(r"[^\n]", " ", src[i:j])); i = j
        elif src.startswith('"""', i):
            j = src.find('"""', i + 3)
            j = n if j == -1 else j + 3
            out.append(re.sub(r"[^\n]", " ", src[i:j])); i = j
        elif c in "\"'":
            j = i + 1
            while j < n and src[j] != c and src[j] != "\n":
                j += 2 if src[j] == "\\" else 1
            j = min(j + 1, n)
            out.append(" " * (j - i)); i = j
        else:
            out.append(c); i += 1
    return "".join(out)


def split_params(text: str):
    parts, depth, cur = [], 0, []
    for ch in text:
        if ch in "<([{": depth += 1
        elif ch in ">)]}": depth -= 1
        if ch == "," and depth == 0:
            parts.append("".join(cur)); cur = []
        else:
            cur.append(ch)
    parts.append("".join(cur))
    return [p.strip() for p in parts if p.strip()]


def looks_like_param(p: str) -> bool:
    """A declaration parameter is `[annotations/final] Type name` (>= 2 tokens)."""
    p = re.sub(r"@[\w.]+(\s*\([^)]*\))?", " ", p)       # strip annotations
    p = re.sub(r"<[^<>]*(<[^<>]*>[^<>]*)*>", "", p)     # strip generics
    p = re.sub(r"\b(final)\b", " ", p)
    return len(re.findall(r"[\w$.\[\]]+(?:\.\.\.)?", p)) >= 2


def find_declarations(code: str):
    for m in re.finditer(r"([A-Za-z_$][\w$]*)\s*\(", code):
        name = m.group(1)
        if name in KEYWORDS:
            continue
        # match closing paren
        depth, i = 1, m.end()
        while i < len(code) and depth:
            depth += {"(": 1, ")": -1}.get(code[i], 0)
            i += 1
        if depth:
            continue
        params_text = code[m.end():i - 1]
        after = code[i:i + 200].lstrip()
        before = code[:m.start()].rstrip()
        prev_tok = re.search(r"([A-Za-z_$][\w$]*|[>\]\)\.=,(])$", before)
        prev = prev_tok.group(1) if prev_tok else ""
        if prev in KEYWORDS - {"default"} and prev != "record":
            continue
        if prev in {".", "=", ",", "("}:
            continue
        is_body = after.startswith("{") or re.match(r"throws\b", after)
        is_abstract = after.startswith(";") and (IDENT.fullmatch(prev) or prev in {">", "]"})
        if not (is_body or is_abstract):
            continue
        params = split_params(params_text)
        if params and not all(looks_like_param(p) for p in params):
            continue
        start = code.count("\n", 0, m.start()) + 1
        end = code.count("\n", 0, i) + 1
        yield name, len(params), start, end


def changed_lines(base: str, head: str):
    """Return {path: set(line numbers)} of added/modified lines in .java files."""
    diff = subprocess.run(
        ["git", "diff", "-U0", "--no-color", "--diff-filter=AMR", f"{base}...{head}", "--", "*.java"],
        capture_output=True, text=True, check=True).stdout
    result, current = {}, None
    for line in diff.splitlines():
        if line.startswith("+++ "):
            current = None if line.endswith("/dev/null") else os.path.normpath(line[6:])
            if current:
                result.setdefault(current, set())
        elif line.startswith("@@") and current:
            m = re.search(r"\+(\d+)(?:,(\d+))?", line)
            start, count = int(m.group(1)), int(m.group(2) or 1)
            result[current].update(range(start, start + count))
    return result


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--max", type=int, default=4)
    ap.add_argument("--root", default=".")
    ap.add_argument("--base", help="base commit; only flag declarations touching lines changed since it")
    ap.add_argument("--head", default="HEAD")
    ap.add_argument("--exclude", default="", help="comma-separated path substrings to skip")
    a = ap.parse_args()
    excludes = [e for e in a.exclude.split(",") if e]

    changed = changed_lines(a.base, a.head) if a.base else None
    if changed is not None:
        print(f"Checking {len(changed)} changed Java file(s)")

    violations = 0
    for dp, dns, fns in os.walk(a.root):
        dns[:] = [d for d in dns if d not in SKIP_DIRS]
        for fn in fns:
            path = os.path.join(dp, fn)
            if not fn.endswith(".java") or any(e in path for e in excludes):
                continue
            rel = os.path.relpath(path, a.root)
            if changed is not None and os.path.normpath(rel) not in changed:
                continue
            with open(path, encoding="utf-8", errors="replace") as f:
                code = blank_comments_and_strings(f.read())
            for name, count, line, end in find_declarations(code):
                if count > a.max:
                    if changed is not None and not changed[os.path.normpath(rel)] & set(range(line, end + 1)):
                        continue
                    violations += 1
                    print(f"::error file={rel},line={line},title=Too many parameters::"
                          f"'{name}' has {count} parameters (max {a.max})")
    print(f"Done: {violations} violation(s).")
    sys.exit(1 if violations else 0)


if __name__ == "__main__":
    main()
