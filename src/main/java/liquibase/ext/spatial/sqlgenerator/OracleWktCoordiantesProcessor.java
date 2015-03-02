package liquibase.ext.spatial.sqlgenerator;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

/**
 * This class process a WKT string to generate the <code>SDO_ELEM_INFO</code>
 * and <code>SDO_ORDINATES</code> to use in <code>SDO_GEOMETRY</code> Object
 * constructor. <br/>
 * This class is based on <em>JTS</em> <code>WKTReader</code> implementation. <br/>
 * This class parses a OGC WKT v1.2 specification (except <code>tim</code>,
 * <code>triangle</code> and <code>polyhedralSurface</code>)
 * 
 */
public class OracleWktCoordiantesProcessor {

   private static final String SDO_ETYPE_POINT = "1";
   private static final String SDO_ETYPE_LINESTRING = "2";
   private static final String SDO_ETYPE_POLYGON_EXT = "1003";
   private static final String SDO_ETYPE_POLYGON_INT = "2003";

   private static final String SDO_INTERPRETATION_POINT = "1";

   private static final String EMPTY = "EMPTY";
   private static final String COMMA = ",";
   private static final String L_PAREN = "(";
   private static final String R_PAREN = ")";
   private static final String NAN_SYMBOL = "NaN";

   private StreamTokenizer tokenizer = null;

   private List<String> elementInfoArray;
   private List<String> ordinatesArray;

   /**
    * Fill <code>elementInfoArray</code> and <code>coordinates</code> based on
    * <code>wkt</code> data
    * 
    * @param wkt
    * @throws IOException
    * @throws IllegalStateException when problem found on WKT format
    */
   public void process(String wkt, List<String> elementInfoArray,
         List<String> coordinates) throws IOException {

      if (tokenizer != null) {
         throw new IllegalStateException(
               "Already processing. Use a new instance.");
      }
      StringReader reader = null;
      try {
         reader = new StringReader(wkt);
         tokenizer = new StreamTokenizer(reader);

         tokenizer.resetSyntax();
         tokenizer.wordChars('a', 'z');
         tokenizer.wordChars('A', 'Z');
         tokenizer.wordChars(128 + 32, 255);
         tokenizer.wordChars('0', '9');
         tokenizer.wordChars('-', '-');
         tokenizer.wordChars('+', '+');
         tokenizer.wordChars('.', '.');
         tokenizer.whitespaceChars(0, ' ');
         tokenizer.commentChar('#');

         this.elementInfoArray = elementInfoArray;
         this.ordinatesArray = coordinates;

         readGeometryTaggedText(null);

      }
      finally {
         if (reader != null) {
            reader.close();
         }
         tokenizer = null;
         this.elementInfoArray = null;
         this.ordinatesArray = null;
      }
   }

   /**
    * Read and load from current position a tagged geometry. <br/>
    * Examples: <code>POINT (1 1)</code>
    * <code>LINESTRING M (1 1 0, 2 2 1)</code>
    * 
    * @param previousDimensions to check dimension of
    *        <code>GEOMETRYCOLLECTION</code> sub items
    * @throws IOException
    */
   private void readGeometryTaggedText(Integer previousDimensions)
         throws IOException {
      String type = null;

      try {
         type = getNextWord();
      }
      catch (IOException e) {
         throw new IllegalArgumentException("Parse error", e);
      }
      if (type == null) {
         throw new IllegalArgumentException(
               "Invalid WKT: geometry type expected");
      }
      type = type.toUpperCase();
      int dimensions = 2;

      // Get next word without move parser pointer
      String nextWord = lookaheadWord();
      if (nextWord == null || nextWord.isEmpty()) {
         // unexpected string
         throw new IllegalArgumentException(
               "Invalid WKT: expected '(', 'Z', 'ZM' or 'M'.");
      }
      else if (L_PAREN.equals(nextWord)) {
         if (type.endsWith("M")) {
            type = type.substring(0, type.length() - 1);
            dimensions++;
         }
         if (type.endsWith("Z")) {
            type = type.substring(0, type.length() - 1);
            dimensions++;
         }
      }
      else {
         // Move parse pointer (to skip word to process)
         nextWord = getNextWord();
         if ("ZM".equalsIgnoreCase(nextWord)) {
            dimensions++;
            dimensions++;
         }
         else if ("Z".equalsIgnoreCase(nextWord)
               || "M".equalsIgnoreCase(nextWord)) {
            dimensions++;
         }
         else {
            // unexpected string
            throw new IllegalArgumentException(
                  "Invalid WKT: Unexpected string: ".concat(nextWord));
         }
      }
      
      // check parent (collection) previous dimension
      if (previousDimensions != null) {
         // Check parent dimensions
         if (previousDimensions != dimensions) {
            throw new IllegalArgumentException(
                  "Invalid WKT: Mixed geometries with different dimensions");
         }

      }

      // Read geometries
      if ("POINT".equalsIgnoreCase(type)) {
         readPointText(dimensions);
      }
      else if ("LINESTRING".equalsIgnoreCase(type)) {
         readLineStringText(dimensions);
      }
      else if ("POLYGON".equalsIgnoreCase(type)) {
         readPolygonText(dimensions);
      }
      else if ("MULTIPOINT".equalsIgnoreCase(type)) {
         readMultiPointText(dimensions);
      }
      else if ("MULTILINESTRING".equalsIgnoreCase(type)) {
         readMultiLineStringText(dimensions);
      }
      else if ("MULTIPOLYGON".equalsIgnoreCase(type)) {
         readMultiPolygonText(dimensions);
      }
      else if ("GEOMETRYCOLLECTION".equalsIgnoreCase(type)) {
         readGeometryCollectionText(dimensions);
      }
      else {
         throw new IllegalArgumentException(String.format(
               "Unknown geometry type: ", type));
      }
   }

   /**
    * Gets next word of parser cursor
    * 
    * @return next work token
    * @throws IOException
    * @throws IllegalArgumentException if next token is not a word
    */
   private String getNextWord() throws IOException {
      return getNextWord(false);
   }

   /**
    * Gets next word of parser cursor
    * 
    * @param optional
    * @return next word token (or null if next token is not a word and
    *         <code>optional</code> is <code>true</code>)
    * @throws IOException
    * @throws IllegalArgumentException if next token is not a word and
    *         <code>optional</code> is <code>false</code>
    */
   private String getNextWord(boolean optional) throws IOException {
      int type = tokenizer.nextToken();
      switch (type) {
      case StreamTokenizer.TT_WORD:

         String word = tokenizer.sval;
         if (word.equalsIgnoreCase(EMPTY))
            return EMPTY;
         return word;

      case '(':
         return L_PAREN;
      case ')':
         return R_PAREN;
      case ',':
         return COMMA;
      }
      if (optional) {
         return null;
      }
      else {
         throw new IllegalArgumentException("Invalid WKT");
      }
   }

   /**
    * Gets next word on parser cursor without move forward it.
    * 
    * @return next token if it's a work token, otherwise <code>null</code>
    * @throws IOException
    */
   private String lookaheadWord() throws IOException {
      String nextWord = getNextWord(true);
      tokenizer.pushBack();
      return nextWord;
   }

   /**
    * Read and load a <code>GEOMETRYCOLLECTION</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readGeometryCollectionText(int dimensions) throws IOException {
      String nextToken = getNextOpener();
      do {
         readGeometryTaggedText(dimensions);
         nextToken = getNextCloserOrComma();
      } while (nextToken.equals(COMMA));

   }

   /**
    * Read and load a <code>MULTIPOINT</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readMultiPointText(int dimensions) throws IOException {
      String nextToken = getNextOpener();
      do {
         addElementInfo(getCurrentOffset(), SDO_ETYPE_POINT, "1");
         loadCoordinate(dimensions);
         nextToken = getNextCloserOrComma();
      } while (nextToken.equals(COMMA));
   }

   /**
    * Read and load a <code>MULTIPOLYGON</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readMultiPolygonText(int dimensions) throws IOException {
      String nextToken = getNextOpener();
      do {
         readPolygonText(dimensions);
         nextToken = getNextCloserOrComma();
      } while (nextToken.equals(COMMA));
   }

   /**
    * Read and load a <code>MULTILINESTRING</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readMultiLineStringText(int dimensions) throws IOException {
      String nextToken = getNextOpener();
      do {
         readLineStringText(dimensions);
         nextToken = getNextCloserOrComma();
      } while (nextToken.equals(COMMA));
   }

   /**
    * @return current ordinatesArray offset
    */
   private int getCurrentOffset() {
      return ordinatesArray.size() + 1;
   }

   /**
    * Read and load a <code>POINT</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readPointText(int dimensions) throws IOException {
      getNextOpener();
      addElementInfo(getCurrentOffset(), SDO_ETYPE_POINT,
            SDO_INTERPRETATION_POINT);
      loadCoordinate(dimensions);
      getNextCloser();
   }

   /**
    * Read and load a <code>LINESTRING</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readLineStringText(int dimensions) throws IOException {
      getNextOpener();
      addElementInfo(getCurrentOffset(), SDO_ETYPE_LINESTRING, "1");
      loadCoordinates(dimensions);
   }

   /**
    * Read and load a <code>POLYGON</code> data
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void readPolygonText(int dimensions) throws IOException {
      // Polygon group opener
      String nextToken = getNextOpener();
      // External polygon starts
      getNextOpener();
      addElementInfo(getCurrentOffset(), SDO_ETYPE_POLYGON_EXT, "1");
      loadCoordinates(dimensions);
      nextToken = getNextCloserOrComma();
      while (nextToken.equals(COMMA)) {
         // Internal polygon starts
         getNextOpener();
         addElementInfo(getCurrentOffset(), SDO_ETYPE_POLYGON_INT, "1");
         loadCoordinates(dimensions);
         nextToken = getNextCloserOrComma();
      }
   }

   /**
    * Read and load a single Geometry coordinate into {@link #ordinatesArray}
    * 
    * @param dimensions of coordinates
    * @throws IOException
    */
   private void loadCoordinate(int dimensions) throws IOException {
      for (int i = 0; i < dimensions; i++) {
         ordinatesArray.add(getNextNumber());
      }
   }

   /**
    * Read and load a list of Geometry coordinate into {@link #ordinatesArray}
    * 
    * @param dimensions of coordinates
    * @throws IOException
    * @return number of coordinates (groups of ordinates) loaded
    */
   private int loadCoordinates(int dimensions) throws IOException {
      String nextToken;
      int count = 0;
      do {
         loadCoordinate(dimensions);
         count++;
         nextToken = getNextCloserOrComma();
      } while (nextToken.equals(COMMA));
      return count;
   }

   /**
    * @return next Opener (left parenthesis) token
    * @throws IOException
    * @throws IllegalArgumentException if founds an empty token (not supported)
    * @throws IllegalArgumentException if founds other kind of token
    */
   private String getNextOpener() throws IOException {
      String nextWord = getNextWord();
      if (nextWord.equals(L_PAREN)) {
         return nextWord;
      }
      else if (nextWord.equals(EMPTY)) {
         throw new IllegalArgumentException("Empty geometries not supported");
      }
      throw new IllegalArgumentException("Invalid WKT: " + EMPTY + " or "
            + L_PAREN + " expected");
   }

   /**
    * @return next valid number token
    * @throws IOException
    * @throws IllegalArgumentException if founds other kind of token or gots a
    *         number format exception
    */
   private String getNextNumber() throws IOException {
      int type = tokenizer.nextToken();
      switch (type) {
      case StreamTokenizer.TT_WORD: {
         if (tokenizer.sval.equalsIgnoreCase(NAN_SYMBOL)) {
            throw new IllegalArgumentException("Invalid WKT: NaN not supported");
         }
         else {
            try {
               // Check it's valid number
               Double.parseDouble(tokenizer.sval);
               // Return original string (avoid lost of precision)
               return tokenizer.sval;
            }
            catch (NumberFormatException ex) {
               throw new IllegalArgumentException(
                     "Invalid WKT: Invalid number: " + tokenizer.sval);
            }
         }
      }
      }
      throw new IllegalArgumentException("Invalid WKT: Expected number");
   }

   /**
    * Adds a triplet data on {@link #elementInfoArray}
    * 
    * @param offset
    * @param sdoEtype
    * @param sdoInterpretation
    */
   private void addElementInfo(int offset, String sdoEtype,
         String sdoInterpretation) {
      this.elementInfoArray.add(String.valueOf(offset));
      this.elementInfoArray.add(sdoEtype);
      this.elementInfoArray.add(sdoInterpretation);
   }

   /**
    * @return next closer (right parenthesis) token
    * @throws IOException
    * @throws IllegalArgumentException if next token is not a closer
    */
   private String getNextCloser() throws IOException {
      String nextWord = getNextWord();
      if (nextWord.equals(R_PAREN)) {
         return nextWord;
      }
      throw new IllegalArgumentException("Invalid WKT: " + R_PAREN
            + " expected");
   }

   /**
    * @return next closer (right parenthesis) or comma token
    * @throws IOException
    * @throws IllegalArgumentException if next token is not a closer nor comma
    */
   private String getNextCloserOrComma() throws IOException {
      String nextWord = getNextWord();
      if (nextWord.equals(COMMA) || nextWord.equals(R_PAREN)) {
         return nextWord;
      }
      throw new IllegalArgumentException("Invalid WKT: " + COMMA + " or "
            + R_PAREN + " expected");
   }
}