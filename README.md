Custom Set Utility for DeckedBuilder's Card Cam
GUI to extract and recombine card data into new sets.

Requires Java 8 (Tested on windows with Java 8)

-

USAGE:

-

Step 1: Generate Card Data Files

First, Launch Decked Builder, go to the card cam page, and, one by one, check each set checkbox, let the set download, and uncheck that set's checkbox. Close Decked Builder.

Launch the included jar file to bring up the program GUI. Click the "Extract Card Files" button. A folder selection dialog should pop up. If this dialog is in Decked Builder's local files
(on my windows machine, it's at C:\Users\<Username>\AppData\Local\deckedbuilder but it may vary) then you may continue. If not, find Decked Builder's local file directory
before continuing.

In this directory should be a folder called "orbs". Select the orbs folder and press "Open" (or the equivalent option) in the dialog.

Extracting the cards may take some time. The progress bar at the bottom of the window will show the progress of the extraction process. A dialog will open after the process is complete;
press "OK" in this dialog to return to the main program.

This step only needs to be performed once.

-

Step 2: Generate the custom set

To specify the cards to be put in the set, enter them, one per line, in the text area in the program window. 
Special characters such as the "Æ" in Æther Adept should be entered as they are.
The cards "Plains", "Island", "Swamp", "Mountain", and "Forest" are excluded from the custom set.

Press the "Save Set" button to bring up a file save dialog, select a directory to save to (defaults to Decked Builder's local files if possible) and a name for the file.

An internet connection is required to export a set, in order to connect to http://api.deckbrew.com/ and find the multiverse ids of each card.

Exporting a set may take a bit of time (though it should be considerably less than the card extraction process).

-

Step 3: Adding the custom set into Decked Builder

Navigate back to the appdata directory for Decked Builder. Go to deckedbuilder\dbdir\dbdir-<Highest number in the directory> and open the setlist.xml file in a text editor.

Before the `</cardsets>` tag at the end of the file, add the following line:
`<cardset name="Your Set Name"           code="IDK" releasedate="2099-01-01" block="Your Category Header"/>`

Change "Your set name" and "Your Category Header" as desired. Take note of the name you change the set name to.

Find the generated out.orb and rename it to the set name used in the setlist xml, with all spaces replaced with underscores. For example, "Your Set Name" would need the file "your_set_name.orb".

Navigate to deckedbuilder\orbs. Copy the renamed orb file into the deckedbuilder\orbs directory.

When Decked Builder is launched, the custom set should be displayed on the cardcam's set selection list.

-

Updating: Whenever decked builder is updated, perform step 1 and the xml editing portion of step 3 again.
