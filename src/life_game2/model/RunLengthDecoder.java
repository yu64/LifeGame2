package life_game2.model;

public class RunLengthDecoder {

	private static final char DEAD = 'b';
	private static final char ALIVE = 'o';
	private static final char LF = '$';

	public static boolean isEncoded(String code)
	{
		code = code.replaceAll("(\\r\\n|\\n|\\r)", "");
		return code.matches("[0-9bo$]*!?");
	}

	public static String decode(String code, char dead, char alive, char lf)
	{
		String numBuffer = "";

		StringBuilder sb = new StringBuilder();

		String deadText = String.valueOf(dead);
		String aliveText = String.valueOf(alive);
		String lfText = String.valueOf(lf);

		code = code.replaceAll("(\\r\\n|\\n|\\r)", "");

		for(char c : code.toCharArray())
		{



			String text = "";
			if(c == RunLengthDecoder.LF)
			{
				text = lfText;
			}

			if(c == RunLengthDecoder.DEAD)
			{
				text = deadText;
			}

			if(c == RunLengthDecoder.ALIVE)
			{
				text = aliveText;
			}

			if(!text.isEmpty())
			{
				int len = 1;
				if(!numBuffer.isEmpty())
				{
					len = Integer.parseInt(numBuffer, 10);
				}

				sb.append(text.repeat(len));
				numBuffer = "";
				continue;
			}


			if(Character.isDigit(c))
			{
				numBuffer += c;
			}
		}


		return sb.toString();
	}
}
