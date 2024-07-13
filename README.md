To use this raytracer
- Download files: I could not include some of my files in the upload because of size constraints, but you can download them here https://graphics.stanford.edu/data/3Dscanrep/, the program only works
with ply files formatted in ascii, if the ply file you are trying to use is in binary, such as the lucy file, then you will have to convert it into ascii, you may also have to rotate it I used meshlab for this.

- Change file names: The program looks for the lucy file with the name lucy_fixed.ply, if you want to run lucy that is what you will need to name your file once its rotated and converted.

- Setup Run Config: Some files, such as the lucy file, require more memory than is initially offered with standard run configs, setting this memory to 8GB should be sufficient.

- Input File Path: When you run the program it will ask you for a filepath in the console, you need to provide it with the full path to the java directory of the project.
