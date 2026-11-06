package io.mopesbox.Animals.Info;

public class Rare {
    public AnimalInfo animal;
    public double chance;
    public int skin;

    public Rare(AnimalInfo ani, double chance, int skin){
        this.animal = new AnimalInfo(ani);
        this.animal.setRare(true);
        this.chance = chance;
        this.skin = skin;
    }

    public Rare(AnimalInfo ani, double chance, int skin, int coins, int exp){
        this(ani, chance, skin);
        this.animal.setRareCoins(coins);
        this.animal.setEXP(exp);
    }
}
