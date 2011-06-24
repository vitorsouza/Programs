#! /bin/bash

# Function tarballs and bzips all contents of a folder.
tarbz2Folder() {
	dir=$1

	# Makes a tarball and compacts it with BZ2.
	echo "Archiving ${dir}..."
	tar --checkpoint=5000 -jcf "backup-of-${dir}.tar.bz2" "$dir"
	echo "done!"
	echo ""
}

### Begginning of the script: ###

# Process all visible folders.
for dir in *; do 
	# Checks if it's a folder.
	if [ -d "$dir" ]; then
		# Some folders should not be backed up like this (should be dealt with separately).
		if [ "Dropbox" != "$dir" ] && [ "Downloads" != "$dir" ] && [ "Ubuntu One" != "$dir" ] && [ "Software" != "$dir" ] && [ "Transfers" != "$dir" ]; then
			tarbz2Folder "$dir"
		else
			echo "ATTENTION: will not backup folder $dir. You should back it up separately!"
		fi
	fi
done

# Process all invisible folders.
for dir in .*; do
	# Checks if it's a folder.
	if [ -d "$dir" ]; then
		# Checks if it's not "." or "..".
		if [ "." != "$dir" ] && [ ".." != "$dir" ]; then
			tarbz2Folder "$dir"
		fi
	else
		# A hidden file is copied with another name so you know it's there on backup procedures.
		echo "Creating copy of hidden file $dir for backup procedures..."
		cp "$dir" "backup-of-${dir}"
		echo ""
	fi
done
