### Begginning of the script: ###

# Reads the parameters from the command line.
baseName=$1

# Tests if the base name was specified.
if [ "$baseName" = "" ]; then
	baseName="myrecording"
fi

# Starts audio recording.
arecord --quiet --file-type wav --rate=44100 > "$baseName.wav" &
pidA=$(ps -ef | grep "[a]record" | awk '{print $2}')
echo "Audio recording started with process ID $pidA"

# Starts recordmydesktop without 
recordmydesktop -fps 25 --no-sound -v_quality 30 -o "$baseName" &
pidV=$(ps -ef | grep "[r]ecordmydesktop" | awk '{print $2}')
echo "Video recording started with process ID $pidV"

# Waits for the user to press enter.
echo ""
echo "Press ENTER to finish."
read nothing

# Kills arecord and sends a Ctrl+C signal to recordmydesktop.
echo "Terminating processes $pidA and $pidV..."
kill -15 $pidA $pidV

# Wait for recordmydesktop to finish converting the video.
wait
echo ""
echo "Converting and merging audio and video"

# Converts the video to avi.
mencoder -ovc lavc -oac mp3lame "$baseName.ogv" -o "$baseName-nosound.avi"

# Converts the audio to mp3.
lame -m j -h --vbr-new -b 128 "$baseName.wav" -o "$baseName.mp3"

# Merges them into the final avi.
mencoder  -ovc copy -oac copy -audiofile "$baseName.mp3" "$baseName-nosound.avi" -o "$baseName-final.avi"

echo ""
echo "DONE! Final video written in file $baseName-final.avi"
