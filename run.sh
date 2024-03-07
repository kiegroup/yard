#!/bin/sh

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <model_file>"
    exit 1
fi

model_file=$1


# printf "Running model $1 with json: $2\n"

java -jar yard-impl1-cli/target/yard-impl1-cli-1.0-SNAPSHOT.jar "$model_file" <<< "$(cat -)"