package main.core;

public enum Difficulty
{
	EASY("EASY_DIFFICULTY_CAPTION"), MEDIUM("MEDIUM_DIFFICULTY_CAPTION"), HARD("HARD_DIFFICULTY_CAPTION");
	
	private String mI18NCaption = null;
	
	private Difficulty(String i18nCaption)
	{
		this.mI18NCaption = i18nCaption;
		return;
	}
	
	public String getI18NCaption()
	{
		return this.mI18NCaption;
	}
	
	public static Difficulty getTypeForName(String name)
	{
		for(Difficulty type : Difficulty.values())
		{
			if(type.name().equalsIgnoreCase(name)){
				return type;
			}
		}
		return null;
	}
};
