package com.oxygenxml.sdksamples.workspace;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import ro.sync.diff.text.DiffEntry;


/**
 * 
 * This class receives a document. It reads
 * character by character and remembers the
 * Elements. Meaning every time it stumbles upon 
 * a beginning tag it parses it and remembers its
 * start and end offset and what type of element it is:
 * ELEMENT, CDATA, DOCTYPE, PI, COMMENT...
 * It passes that element to the ParseElemet Class.
 * 
 * @author intern3
 *
 */
public class XMLMainParser {
	
	private ContentListener contentListener;
	StringBuilder resultToCheckIfItReadsCorrectly;
	
	/**
	 * Every time I read an element I want to know
	 * what index it was, so ReaderWithIndex is a
	 * wrapper to Reader that remembers the current
	 * index of the current read character
	 * @author intern3
	 *
	 */
	class ReaderWithIndex extends Reader{
		
		private int index;
		private Reader innerReader;

		public int getIndex(){
			return index;
		}
		
		public ReaderWithIndex(Reader innerReader) {
			this.innerReader = innerReader;
			this.index = -1;
		}
		
		/**
		 * Increments by the number of read characters (which in my
		 * case will always be one) the index so I know what index has
		 * the last read character 
		 */
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			index += len;
			return innerReader.read(cbuf, off, len);
		}

		@Override
		public void close() throws IOException {
			innerReader.close();
			
		}
		
	}

	
	
	
	
	
	XMLMainParser(){
		resultToCheckIfItReadsCorrectly = new StringBuilder();
	}
	
	
	/**
	 * I am setting the Listener to pass it on to the parser
	 * @param contentListener
	 */
	public void setContentListener(ContentListener contentListener) {
		this.contentListener = contentListener;
	}
	
	
	/**
	 * Passes the File and wraps it into a ReaderWithIndex
	 * I then initialize the parser and set it with the
	 * current Listener
	 * I am reading the current tag:
	 * NOTE: A tag could be between "> <", a TextField
	 * Then I am passing the result to the parser
	 */
	public void parseInputIntoHTMLFormat(Reader read) {
		int lastCharacter = Integer.MIN_VALUE;
		try {
			ReaderWithIndex reader = new ReaderWithIndex(read);
			ParseElement parser = new ParseElement();
			parser.setContentListener(contentListener);

			do {
				CurrentReadElement currentElement = new CurrentReadElement();
				
				lastCharacter = readTag(currentElement, reader, lastCharacter);

				parser.setCurrentElement(currentElement);
				parser.parse();
				

				resultToCheckIfItReadsCorrectly.append(currentElement.elementContent.toString());


			} while (lastCharacter != -1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * I split the tags in two categories : Tags that begin
	 * with a startTag "<" or tags that begin 
	 * with an endTag ">"
	 * I set the beginOffset before properly beginning to read through 
	 * any elements.
	 * @param currentElement
	 * @param reader
	 * @param currentCharacter
	 * @return
	 * @throws IOException
	 */
	private int readTag(CurrentReadElement currentElement, ReaderWithIndex reader, int currentCharacter) throws IOException {
		
		if (currentCharacter == Integer.MIN_VALUE) { /*The first time I enter this method 
													there is no read character, so I read it one time here*/
			currentCharacter = reader.read();
		}
		
		
		if (currentCharacter == '<') {
			currentElement.elementContent.append((char) currentCharacter);
			currentElement.beginOffset = reader.getIndex();
			currentCharacter = readElementWithBeginTag(currentElement, reader);
		} else {
			currentElement.beginOffset = reader.getIndex()+1; /*Adding a 1 because it returns the end of the last element
			 													and the index would duplicate*/
			currentCharacter = readElementWithEndTag(currentElement, reader);  
		}

		return currentCharacter;
		
	}
	
/**
 * I go through characters one by one and compare them with the given string.
 * If there is no difference it return null, otherwise it returns the string
 * up until the difference occurs
 * @param reader
 * @param comparedToThis
 * @return
 * @throws IOException
 */
	private String checkTwoWords(ReaderWithIndex reader, String comparedToThis) throws IOException{
		
		StringBuilder buffer = new StringBuilder();
		int currentCharacter = 0;
		
		int index = 1;
		while(index < comparedToThis.length() && (currentCharacter = reader.read()) != -1){
			
			if((char)currentCharacter != comparedToThis.charAt(index++)){
				return (buffer.toString() + (char)currentCharacter);
			}
			buffer.append(currentCharacter);
		}
	

		return null;
	}

	
	
	
	/**
	 * Each Tag that starts with a beginingTag could be more than one
	 * type of Node. So I identify the type of the node by checking
	 * the beginning tag with the ones of each element and if it fits
	 * I set it.
	 * Before I even enter the function I KNOW that I have a beginning tag
	 * so I have to check the very next character/characters.
	 * 
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int findNodeType (CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
		int currentCharacter = 0;
		NodeType currentTag = null;

		currentCharacter = reader.read();

		if ((char) currentCharacter == '/') { 
			currentTag = NodeType.ELEMENT_CLOSE;
			currentElement.elementContent.append("/");

		} else if ((char) currentCharacter != '!') {/*If the next character is not "/" it could be
													anything at this point, so I have to check it*/

			if ((char) currentCharacter != '?') {
				currentTag = NodeType.ELEMENT;
				currentElement.elementContent.append((char) currentCharacter);

			} else {
				currentTag = NodeType.PI;
				currentElement.elementContent.append("?");

			}
		} else {                                 /*After I know that my tag begins with "!" it means I could
		 										have any of the next tags: CDATA, DOCTYPE, COMMENT or a misspelled 
		 										permutation of characters in which case i should recognize it as an
		 										element*/
			currentCharacter = reader.read();

			if ((char) currentCharacter == 'D') { /*The problem is if by choice I want to give an element
			 										a name like !DOCTIPE, I cannot focus only on the first 
			 										character after the "!" symbol, so I have to check the 
			 										whole word */

				String charactersInTheWordSoFar = checkTwoWords(reader, "DOCTYPE");

				if (charactersInTheWordSoFar == null) {
					currentElement.elementContent.append("DOCTYPE");
					currentTag = NodeType.DOCTYPE;
				} else {
					currentElement.elementContent.append("D" + charactersInTheWordSoFar);
					currentTag = NodeType.ELEMENT;
				}

			} else if ((char) currentCharacter == '[') {

				String charactersInTheWordSoFar = checkTwoWords(reader, "[CDATA[");

				if (charactersInTheWordSoFar == null) {
					currentTag = NodeType.CDATA;
					currentElement.elementContent.append("[CDATA[");
				} else {
					currentElement.elementContent.append("[" + charactersInTheWordSoFar);
					currentTag = NodeType.ELEMENT;
				}

			} else if ((char) currentCharacter == '-') {

				String charactersInTheWordSoFar = checkTwoWords(reader, "--");

				if (charactersInTheWordSoFar == null) {
					currentTag = NodeType.COMMENT;
					currentElement.elementContent.append("--");
				} else {
					currentElement.elementContent.append("-" + charactersInTheWordSoFar);
					currentTag = NodeType.ELEMENT;
				}
			}
		}

		currentElement.type = currentTag;
			
		
		return currentCharacter;
	}

	
	
	
	
	
	/**
	 * The Element with an End Tag could be a TextField or the empty spaces
	 * between two tags. If the text between two tags is filled with just
	 * empty spaces: white spaces, new lines, tabs, it means there is no
	 * TextField, just data that has to be pated into the result
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int readElementWithEndTag(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
		int currentCharacter = 0;
		boolean mayHaveAttribute = false;
		
		currentElement.type = NodeType.EMPTYDATA;
		
		while((currentCharacter = reader.read()) != -1){
			
			if((char)currentCharacter == '\r'){  /*This character is accompanied by the "\n" tag and
			 									while the two of them only denote the appearance of
			 									an ENTER, the list of offsets skips this Element, so
			 									I am skipping it as well. Also I am decrementing the current
			 									index because I am treating the '/r' character as if it
			 									would not exist*/
				reader.index--;
			}
			
			if(currentCharacter == '<'){
				break;
			}
			
			
			if(checkForTabsNewLinesOrWhiteSpaces((char)currentCharacter)){
				mayHaveAttribute = true;
			}else if(mayHaveAttribute){
				currentElement.type = NodeType.TEXTFIELD;
			}
			
			if((char)currentCharacter != '\r'){  
				currentElement.elementContent.append((char)currentCharacter);
			}
			
		}
		
		currentElement.endOffset = reader.getIndex()-1;
		return currentCharacter;
	}

	
	
	
	
	
	/**
	 * Depending on which type of Node it is, I parse it accordingly
	 * and in the end I just set the endOffset
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int readElementWithBeginTag(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
			
		int currentCharacter = findNodeType(currentElement, reader);
			
		switch (currentElement.type) {
		case ELEMENT:	
			currentCharacter = addElement(currentElement, reader);
			break;
		case ELEMENT_CLOSE:
			currentCharacter = addClosingElement(currentElement, reader);
			break;
		case PI:
			currentCharacter = addProcessingInformation(currentElement, reader);
			break;
		case DOCTYPE:
			currentCharacter = addDoctype(currentElement, reader);
			break;
		case CDATA:
			currentCharacter = addCdata(currentElement, reader);
			break;
		case COMMENT:
			currentCharacter = addComment(currentElement, reader);
			break;
		default:
			break;
		}
		
		currentElement.endOffset = reader.getIndex();
		
		return currentCharacter;
	}
	
	
	
	
	
	/**
	 * Checks if the given character is a white space, a new line or a tab and
	 * returns the answer;
	 * @param character
	 * @return
	 */
	private boolean checkForTabsNewLinesOrWhiteSpaces(char character){

		if(character == ' ' || character == '\n' || character == '\t' || character == '\r'){
			return true;
		}
		return false;
	}
	
	
	
	
	
	/**
	 * Reads character by character until stumbling upon an end tag
	 * and also tells if the element has an attribute
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int addElement(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
		
		int currentCharacter = 0;
		boolean mayHaveAttribute = false;
		
		while((currentCharacter = reader.read()) != -1){
			
			if(currentCharacter == '>'){
				currentElement.elementContent.append((char)currentCharacter);
				break;
			}
			
			if(checkForTabsNewLinesOrWhiteSpaces((char)currentCharacter)){
				mayHaveAttribute = true;
			}else if(mayHaveAttribute){
				currentElement.isElementAndHasAttribute = true;
			}
			
			currentElement.elementContent.append((char)currentCharacter);
		}
		
		
		return currentCharacter;
	}
	
	
	
	
	
	/**
	 * Parses the element until it meets the closing tag 
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int addClosingElement(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
		
		int currentCharacter = 0;
		
		
		while((currentCharacter = reader.read()) != -1){
			
			if(currentCharacter == '>'){
				currentElement.elementContent.append((char)currentCharacter);
				break;
			}
			
			currentElement.elementContent.append((char)currentCharacter);
			
		}
		
		
		return currentCharacter;
	}
	
	
	
	
	/**
	 * Parses the element until it meets the closing tag 
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int addComment(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
		
		int currentCharacter = 0;

		
		while((currentCharacter = reader.read()) != -1){
			
			if(currentCharacter == '>'){
				currentElement.elementContent.append((char)currentCharacter);
				break;
			}
			
			currentElement.elementContent.append((char)currentCharacter);
			
		}
		
		
		return currentCharacter;
		
	}
	
	
	
	
	
	/**
	 * Parses the element until it meets the closing tag 
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int addProcessingInformation(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
				

		int currentCharacter = 0;

		while ((currentCharacter = reader.read()) != -1) {

			if ((char) currentCharacter == '?') {

				char nextCharacter = (char) reader.read();

				if ((char) nextCharacter == '>') {
					currentElement.elementContent.append("?>");
					break;
				} else {
					currentElement.elementContent.append((char) currentCharacter + "" + nextCharacter);
				}

			} else {
				currentElement.elementContent.append((char) currentCharacter);
			}
		}

		return currentCharacter;
		
	}
	
	
	
	
	
	/**
	 * Parses the element until it meets the closing tag 
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int addCdata(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
			

		int currentCharacter = 0;

		while ((currentCharacter = reader.read()) != -1) {
			
			if ((char) currentCharacter == ']') {
				
				char auxiliarCharacter1 = (char) reader.read();
				if (auxiliarCharacter1 == ']') {
					
					char auxiliarCharacter2 = (char) reader.read();
					if (auxiliarCharacter2 == '>') {
						
						currentElement.elementContent.append("]]>");
						break;
					} else {
						currentElement.elementContent
								.append((char) currentCharacter + "" + auxiliarCharacter1 + "" + auxiliarCharacter2);
					}
				} else {
					currentElement.elementContent.append((char) currentCharacter + "" + auxiliarCharacter1);
				}
			} else {

				currentElement.elementContent.append((char) currentCharacter);
			}
		}

		return currentCharacter;
	}
	
	
	
	
	
	
	/**
	 * Parses the element until it meets the closing tag 
	 * @param currentElement
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int addDoctype(CurrentReadElement currentElement, ReaderWithIndex reader) throws IOException{
				
		int currentCharacter = 0;

		boolean closedCounter = true; /*
										 * there is the possibility that a tag
										 * may appear inside commas, in which
										 * case it shall not be counted
										 */

		while ((currentCharacter = reader.read()) != -1) {

			if ((char) currentCharacter == '\"' || (char) currentCharacter == '\"') {
				closedCounter = !closedCounter;
			}

			if ((char) currentCharacter == '>' && closedCounter) {
				currentElement.elementContent.append(">");
				break;
			}

			currentElement.elementContent.append((char) currentCharacter);

		}

		// System.out.println(currentCharacter);

		return currentCharacter;
	}
	
	
	
	
	public static void main(String[] args) {
		try {
			FileReader in = new FileReader("html.in");
			Reader buf = (Reader) (new BufferedReader(in));

			XMLMainParser parser = new XMLMainParser();
			HTMLContentGenerator generator = new HTMLContentGenerator(null, true);
			parser.setContentListener(generator);
			parser.parseInputIntoHTMLFormat(buf);
			System.out.println(parser.resultToCheckIfItReadsCorrectly);
			System.out.println(generator.getResultedText());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}













