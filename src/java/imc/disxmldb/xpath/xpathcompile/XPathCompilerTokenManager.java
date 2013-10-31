/* Generated By:JavaCC: Do not edit this line. XPathCompilerTokenManager.java */
package imc.disxmldb.xpath.xpathcompile;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.utils.LatencyTracker;
import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.index.filter.XMLLocalDocFilter;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathProcessorV2.XPathError;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.FunctionDef;
import imc.disxmldb.xpath.function.FunctionPool;

/** Token Manager. */
public class XPathCompilerTokenManager implements XPathCompilerConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x440000L) != 0L)
            return 37;
         if ((active0 & 0x60L) != 0L)
            return 15;
         if ((active0 & 0x4000L) != 0L)
            return 38;
         if ((active0 & 0x1000000000L) != 0L)
            return 11;
         if ((active0 & 0x3a000000L) != 0L)
            return 39;
         if ((active0 & 0x880000L) != 0L)
            return 40;
         return -1;
      case 1:
         if ((active0 & 0x80000L) != 0L)
            return 2;
         if ((active0 & 0x40000L) != 0L)
            return 7;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 34:
         jjmatchedKind = 23;
         return jjMoveStringLiteralDfa1_0(0x80000L);
      case 39:
         jjmatchedKind = 22;
         return jjMoveStringLiteralDfa1_0(0x40000L);
      case 40:
         return jjStopAtPos(0, 30);
      case 41:
         return jjStopAtPos(0, 31);
      case 42:
         return jjStopAtPos(0, 7);
      case 43:
         return jjStartNfaWithStates_0(0, 5, 15);
      case 44:
         return jjStopAtPos(0, 32);
      case 45:
         return jjStartNfaWithStates_0(0, 6, 15);
      case 46:
         jjmatchedKind = 25;
         return jjMoveStringLiteralDfa1_0(0x38000000L);
      case 47:
         jjmatchedKind = 8;
         return jjMoveStringLiteralDfa1_0(0x4000000L);
      case 59:
         return jjStopAtPos(0, 35);
      case 60:
         jjmatchedKind = 10;
         return jjMoveStringLiteralDfa1_0(0x800L);
      case 61:
         return jjStopAtPos(0, 9);
      case 62:
         jjmatchedKind = 12;
         return jjMoveStringLiteralDfa1_0(0x2000L);
      case 64:
         return jjStartNfaWithStates_0(0, 36, 11);
      case 91:
         return jjStopAtPos(0, 33);
      case 93:
         return jjStopAtPos(0, 34);
      case 95:
         return jjStartNfaWithStates_0(0, 14, 38);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 34:
         if ((active0 & 0x80000L) != 0L)
            return jjStartNfaWithStates_0(1, 19, 2);
         break;
      case 39:
         if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(1, 18, 7);
         break;
      case 46:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
      case 47:
         if ((active0 & 0x4000000L) != 0L)
            return jjStopAtPos(1, 26);
         else if ((active0 & 0x10000000L) != 0L)
         {
            jjmatchedKind = 28;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000L);
      case 61:
         if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(1, 11);
         else if ((active0 & 0x2000L) != 0L)
            return jjStopAtPos(1, 13);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 47:
         if ((active0 & 0x8000000L) != 0L)
            return jjStopAtPos(2, 27);
         else if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(2, 29);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0x0L, 0xffffffffffff0000L, 0xffffffffL, 0x0L
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 37;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 40:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  else if (curChar == 34)
                  {
                     if (kind > 24)
                        kind = 24;
                  }
                  if (curChar == 34)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 38:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(35, 36);
                  else if (curChar == 40)
                  {
                     if (kind > 40)
                        kind = 40;
                  }
                  else if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 33;
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 37)
                        kind = 37;
                     jjCheckNAdd(30);
                  }
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 15)
                        kind = 15;
                     jjCheckNAddStates(3, 8);
                  }
                  else if ((0x280000000000L & l) != 0L)
                     jjCheckNAddStates(9, 11);
                  else if (curChar == 46)
                     jjCheckNAddTwoStates(16, 28);
                  else if (curChar == 39)
                     jjCheckNAddStates(12, 14);
                  else if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  break;
               case 15:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  else if (curChar == 46)
                     jjCheckNAdd(16);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 15)
                        kind = 15;
                     jjCheckNAdd(14);
                  }
                  break;
               case 37:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(12, 14);
                  else if (curChar == 39)
                  {
                     if (kind > 24)
                        kind = 24;
                  }
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 39:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(28, 24);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAdd(16);
                  }
                  break;
               case 1:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 2:
                  if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  break;
               case 3:
                  if (curChar == 34)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 4:
                  if (curChar == 34 && kind > 24)
                     kind = 24;
                  break;
               case 5:
               case 7:
                  if (curChar == 39)
                     jjCheckNAddStates(12, 14);
                  break;
               case 6:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(12, 14);
                  break;
               case 8:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 9:
                  if (curChar == 39 && kind > 24)
                     kind = 24;
                  break;
               case 12:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 13:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAddStates(9, 11);
                  break;
               case 14:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAdd(14);
                  break;
               case 16:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAdd(16);
                  break;
               case 17:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 18:
                  if (curChar != 46)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAdd(19);
                  break;
               case 19:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAdd(19);
                  break;
               case 20:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjCheckNAddStates(3, 8);
                  break;
               case 21:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(15, 17);
                  break;
               case 22:
                  if (curChar == 46)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 23:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 25:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(26);
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(26);
                  break;
               case 27:
                  if (curChar == 46)
                     jjCheckNAddTwoStates(16, 28);
                  break;
               case 28:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(28, 24);
                  break;
               case 30:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(30);
                  break;
               case 31:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 32:
                  if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 33;
                  break;
               case 34:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjstateSet[jjnewStateCnt++] = 34;
                  break;
               case 35:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(35, 36);
                  break;
               case 36:
                  if (curChar == 40 && kind > 40)
                     kind = 40;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 40:
               case 1:
                  jjCheckNAddStates(0, 2);
                  break;
               case 38:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(35, 36);
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 37)
                        kind = 37;
                     jjCheckNAdd(30);
                  }
                  break;
               case 0:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 37)
                        kind = 37;
                     jjCheckNAddStates(18, 22);
                  }
                  else if (curChar == 64)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 37:
               case 6:
                  jjCheckNAddStates(12, 14);
                  break;
               case 10:
                  if (curChar == 64)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 11:
               case 12:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(12);
                  break;
               case 24:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(23, 24);
                  break;
               case 29:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAddStates(18, 22);
                  break;
               case 30:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(30);
                  break;
               case 31:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 33:
               case 34:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAdd(34);
                  break;
               case 35:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(35, 36);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 40:
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(0, 2);
                  break;
               case 38:
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 37)
                        kind = 37;
                     jjCheckNAdd(30);
                  }
                  if (jjCanMove_5(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(31, 32);
                  if (jjCanMove_8(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(35, 36);
                  break;
               case 0:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAddStates(18, 22);
                  break;
               case 37:
               case 6:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(12, 14);
                  break;
               case 11:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(12);
                  break;
               case 12:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(12);
                  break;
               case 30:
                  if (!jjCanMove_4(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(30);
                  break;
               case 31:
                  if (jjCanMove_5(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 33:
                  if (!jjCanMove_6(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAdd(34);
                  break;
               case 34:
                  if (!jjCanMove_7(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAdd(34);
                  break;
               case 35:
                  if (jjCanMove_8(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(35, 36);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 37 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   1, 3, 4, 14, 17, 18, 21, 22, 24, 14, 15, 17, 6, 8, 9, 21, 
   22, 24, 30, 31, 32, 35, 36, 25, 26, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_3(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_4(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_5(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_6(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_7(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_8(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, "\53", "\55", "\52", "\57", "\75", "\74", 
"\74\75", "\76", "\76\75", "\137", null, null, null, "\47\47", "\42\42", null, null, 
"\47", "\42", null, "\56", "\57\57", "\56\57\57", "\56\57", "\56\56\57", "\50", 
"\51", "\54", "\133", "\135", "\73", "\100", null, null, null, null, null, };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
};
static final long[] jjtoToken = {
   0x1fffffcffe1L, 
};
static final long[] jjtoSkip = {
   0x1eL, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[37];
private final int[] jjstateSet = new int[74];
protected char curChar;
/** Constructor. */
public XPathCompilerTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public XPathCompilerTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 37; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
