#!/bin/bash

# Define paths
SOURCE_PATH="/data/local/tmp/libgo.so"
DEST_PATH="/data/local/tmp/libgo.so"
SELINUX_MODULE="/data/local/tmp/go_policy.pp"

# Check if the source file exists
if [ ! -f "$SOURCE_PATH" ]; then
    echo "Source file $SOURCE_PATH does not exist"
    exit 1
fi

# Change permissions and ownership
chmod 644 "$DEST_PATH"
chown root:root "$DEST_PATH"

# Install SELinux policy module if it exists
if [ -f "$SELINUX_MODULE" ]; then
    semodule -i "$SELINUX_MODULE"
    if [ $? -ne 0 ]; then
        echo "Failed to install SELinux policy module"
        exit 1
    fi
fi

# Reload SELinux policy
setenforce 0
setenforce 1

echo "libgo.so permissions set and SELinux policy reloaded"
