#!/bin/bash
function print_array {
  for word in "$@"; do
    echo "    $word";
  done
}

args=("$@")
echo "args... : ${args[*]}"
print_array "${args[@]}"
a_args=("${args[@]}")
echo "a args... : ${a_args[*]}"
print_array "${a_args[@]}"

#b=("${args[@]}")

n_args=("$@") && n_args=("${n_args[@]:1}")
echo "n args... : ${n_args[*]}"
print_array "${n_args[@]}"
