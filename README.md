Android App for the Hearing Impaired
====================================

For detecting the frequencies of the live input, I am using a library from the Astronomical Institute of Leithbridge. Finding the frequency is done using an Async Task so that the processing is done in the background and is different from the UI thread. The frequency spectrum of the microphone is plotted on the display.

The code has an activity for online training using which one can add sounds to the database to compare the incoming signal. The new sounds get stored in the SharedPreferences.
