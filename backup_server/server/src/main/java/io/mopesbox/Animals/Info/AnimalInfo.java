package io.mopesbox.Animals.Info;

import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.Utils.BiomeType;

public class AnimalInfo {
    private AnimalType animalType;
    private int animalSpecies = 0;
    private int animalSubSpecies = 0;
    private int rareCoins = 0;
    private boolean isRare = false;



    private BiomeType biome = BiomeType.LAND;



    public int tier = 1;

    public boolean isAni = false;

    public PlayerAbility getAbility() {
        return ability;
    }

    private PlayerAbility ability = new PlayerAbility(0);


    public int getTier() {
        return tier;
    }

    public AnimalInfo(AnimalInfo copied){
        this.animalSpecies = copied.animalSpecies;
        this.ability = new PlayerAbility(copied.ability.getType());
        this.animalType = copied.animalType;
        this.tier = copied.tier;
        this.biome = copied.biome;
        this.isAni = copied.isAni;
        this.isRare = copied.isRare;
        this.setRareCoins(copied.rareCoins());
        this.setEXP(copied.getEXP());
    }

    public AnimalInfo(AnimalType animalType, BiomeType biome,int tier){
        this.animalSpecies = 0;
        this.animalType = animalType;
        this.biome = biome;
        this.tier = tier;
    }

    public AnimalInfo(AnimalType animalType, BiomeType biome,int tier,int abilityType){
        this.animalSpecies = 0;
        this.animalType = animalType;
        this.biome = biome;
        this.tier = tier;
        if(abilityType != 0) this.ability = new PlayerAbility(abilityType);
    }

    public AnimalInfo(AnimalType animalType, BiomeType biome,int tier,int abilityType, int skin){
        this.animalSpecies = skin;
        this.animalType = animalType;
        this.biome = biome;
        this.tier = tier;
        if(abilityType != 0) this.ability = new PlayerAbility(abilityType);
    }

    public AnimalInfo(AnimalType animalType, BiomeType biome,int tier,int abilityType, int skin, AnimalInfo rareInstance){
        this(animalType, biome, tier, abilityType, skin);
        this.setRareCoins(rareInstance.rareCoins());
        this.setEXP(rareInstance.getEXP());
    }

    public AnimalInfo(AnimalType animalType, BiomeType biome,int tier,int abilityType, int skin, boolean ani){
        this.animalSpecies = skin;
        this.animalType = animalType;
        this.biome = biome;
        this.isAni = ani;
        this.tier = tier;
        if(abilityType != 0) this.ability = new PlayerAbility(abilityType);
    }

    public AnimalInfo(AnimalType animalType, BiomeType biome,int tier, PlayerAbility ability){
        this.animalSpecies = 0;
        this.animalType = animalType;
        this.biome = biome;
        this.tier = tier;
        this.ability = ability;
    }

    public AnimalInfo(AnimalType animalType,int animalSpecies,BiomeType biome) {
        this.animalSpecies = animalSpecies;
        this.animalType = animalType;
        this.biome = biome;

    }

    public AnimalInfo(AnimalType animalType,BiomeType biome,PlayerAbility ability){
        this.animalSpecies = 0;
        this.animalType = animalType;
        this.biome = biome;

        this.ability = ability;
    }

    public int getAnimalType() {
        return animalType.ordinal();
    }

    public boolean isRare() {
        return isRare;
    }
    public boolean isDanger(AnimalInfo animal) {
        if (this.getType().equals(AnimalType.DUCK) && animal.getType().equals(AnimalType.DUCKLING))
            return false;
        int maxTierDanger = (3 + animal.getTier() * 2);
        if (this.getTier() > animal.getTier() && this.getTier() <= maxTierDanger)
            return true;
        return false;
    }

    public boolean isEdible(Animal animal) {
        if (this.getType().equals(AnimalType.DUCKLING) && animal.getInfo().getType().equals(AnimalType.DUCK))
            return false;
        //    if(this.getType().equals(AnimalType.DUCK))
        //    return false;
        //if(animal.getInfo().getType().equals(AnimalType.DUCK) && this.getTier() >= 7)
        //    return true;
        int minTierEdible = (int) Math.round(animal.getInfo().getTier() - (animal.getInfo().getTier() / 2 - 3));
        if (this.getType().equals(AnimalType.DUCKLING) && animal.getInfo().getTier() >= 4
                && animal.getInfo().getTier() <= 7)
            return true;
        if (this.getTier() < animal.getInfo().getTier() && this.getTier() >= minTierEdible)
            return true;
        if (this.getType().equals(AnimalType.SNAIL) && animal.getInfo().getTier() >= 9)
            return true;
        if (this.getType().equals(AnimalType.FROG) && animal.getInfo().getTier() >= 10)
            return true;
        if (this.getType().equals(AnimalType.DUCK) && animal.getInfo().getTier() >= 7)
            return true;
        if (animal.getInfo().isDanger(this))
            return true;
        return false;
    }
        public boolean isBiteable(Animal animal) {
        if (this.getType().equals(AnimalType.DUCK) && animal.getInfo().getType().equals(AnimalType.DUCKLING))
            return false;
            if(animal.getInfo().getType().equals(AnimalType.HONEYBEE) || this.getType().equals(AnimalType.HONEYBEE)) return false;
        if (this.getTier() >= 15 && animal.getInfo().getTier() >= 15)
            return true;
        if (animal.getInfo().isDanger(this))
            return true;
        return false;
    }

    public int rareCoins() {
        return rareCoins;
    }

    public void setRare(boolean a) {
        isRare = a;
    }

    public void setRareCoins(int a) {
        rareCoins = a;
    }

    private int eXP = 0;

    public void setEXP(int a) {
        eXP = a;
    }

    public int getEXP() {
        return eXP;
    }

    public AnimalType getType() { return animalType;}

    public int getAnimalSubSpecies() {
        return animalSubSpecies;
    }

    public int getAnimalSpecies() {
        return animalSpecies;
    }

    public int getBiome() {
        return biome.ordinal();
    }

    public void setSkin(int s) {
        this.animalSpecies = s;
    }

    public int getSkin() {
        return this.animalSpecies;
    }


}
