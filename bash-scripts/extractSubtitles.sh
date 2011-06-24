#! /bin/bash

# Function that processes the subtitles in a specific folder.
processSubtitles() {
	dir=$1
	curDir=$(pwd)

	# Goes to the folder and processes all files with subtitle extension.
	cd "$dir"
	for subFile in ${SUBTITLE_EXTENSIONS}; do 
		if [ -f "$subFile" ]; then
			# Converts from ISO8859-1 to UTF-8.
			iso2utf8 "$subFile";

			# Moves the subtitle file to the current folder, naming it using the movie name and a counter.
			mv "$subFile" "$curDir/$movieName-$subtitleCount-$subFile"
			subtitleCount=`expr $subtitleCount + 1`
		fi;
	done;
	cd "$curDir"
}

### Begginning of the script: ###

# Constants and pre-defined variables.
SUBTITLE_EXTENSIONS="*.SRT *.srt *.SUB *.sub"
subtitleCount=1

# Reads the parameters from the command line.
movieName=$1

# Tests if the name of the movie was specified.
if [ "$movieName" = "" ]; then
	movieName="Untitled.Movie"
fi

# Process all RAR files.
for file in *.rar; do 
	if [ -f "$file" ]; then
		# Extracts the files into a temporary directory.
		extractDir="/tmp/extractSub_$file"
		if [ ! -d "$extractDir" ]; then mkdir $extractDir; fi;
		unrar e -o+ "$file" "$extractDir/" &> /dev/null;

		# Process the subtitles in there.
		processSubtitles $extractDir

		# Removes the archive.
		rm "$file"
	fi;
done; 

# Process all ZIP files.
for file in *.zip; do
	if [ -f "$file" ]; then
		# Extracts the files into a temporary directory.
		extractDir="/tmp/extractSub_$file"
		if [ ! -d "$extractDir" ]; then mkdir $extractDir; fi;
		unzip -o "$file" -d "$extractDir/" &> /dev/null; 


		# Process the subtitles in there.
		processSubtitles $extractDir

		# Removes the archive.
		rm "$file"
	fi;
done; 

