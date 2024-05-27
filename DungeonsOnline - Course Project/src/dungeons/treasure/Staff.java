package dungeons.treasure;

import dungeons.entities.position.GamePosition;

public class Staff extends Treasure {

    private final int mana;
    private final int spellDamage;

    private final String name;

    private static final int MANA_MULTIPLIER = 2;
    private static final double SPELL_DAMAGE_MULTIPLIER = 2.2;

    public Staff(int level, GamePosition gamePosition) {
        super(level, gamePosition);
        mana = MANA_MULTIPLIER * level;
        spellDamage = (int) (SPELL_DAMAGE_MULTIPLIER * level);
        name = super.getType() + " Staff";
    }

    public int getMana() {
        return mana;
    }

    public int getSpellDamage() {
        return spellDamage;
    }

    @Override
    public String toString() {
        return  "Name: " + name + " Level:" + getLevel() + " Spell Damage:" + spellDamage + " Mana:" + mana;
    }

    @Override
    public int getDamage() {
        return 0;
    }
}
