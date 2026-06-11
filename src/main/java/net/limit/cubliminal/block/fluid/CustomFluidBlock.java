package net.limit.cubliminal.block.fluid;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.limit.cubliminal.util.ColorManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;

public class CustomFluidBlock extends FluidBlock {
    private static final Map<FlowableFluid, CustomFluidBlock> FLUIDS = Maps.newIdentityHashMap();
    protected final CustomFluidBlock.Settings settings;
    private final FlowableFluid fluid;

    public CustomFluidBlock(FlowableFluid fluid, AbstractBlock.Settings settings, CustomFluidBlock.Settings fluidSettings){
        super(fluid, settings);
        this.settings = fluidSettings;
        this.fluid = fluid;
        FLUIDS.put(fluid, this);
    }

    /**
     * An overrideable method to create a list of status effects.
     * Default will be an empty list.
     * @return Status effect list
     */
    protected StatusEffectInstance[] applyEffectsToEntities(){
        return new StatusEffectInstance[0];
    }

    public static Iterable<CustomFluidBlock> getAll(){
        return Iterables.unmodifiableIterable(FLUIDS.values());
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.settings.primaryColor.getHexColor() : 0;
    }

    public ColorManager getColor(){
        return this.settings.primaryColor;
    }

    /**
     * Gets the flowable fluid
     * @return The flowable fluid object.
     */
    public FlowableFluid getFluid(){
        return this.fluid;
    }

    /**
     * Sets all listed effects onto living entities.
     * @param effects The list of status effects.
     * @param living The living entity to apply the effects onto.
     */
    private void setEffectsOnEntities(StatusEffectInstance[] effects, LivingEntity living){
        // Loops through every effect.
        for (StatusEffectInstance effect : effects){
            // Apply the interation of effect to the living entity.
            living.addStatusEffect(effect);
        }
    }

    /**
     * Gets the settings container.
     * @return The settings container.
     */
    public CustomFluidBlock.Settings getFluidSettings(){
        return this.settings;
    }

    /**
     * When an entity collides with the fluid block, it will apply all listed effects to thy entity.
     */
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        // Checks to see if the entity is a living entity.
        if (entity instanceof LivingEntity living){
            // Ensures only players not in creative gets the effects.
            if (entity instanceof PlayerEntity player){
                if (!player.isCreative()){
                    setEffectsOnEntities(applyEffectsToEntities(), living);
                }
            } else {
                // Otherwise, apply effects on all other entities.
                setEffectsOnEntities(applyEffectsToEntities(), living);
            }
        }
    }

    public static class Settings {
        boolean useDefaultFluidPhysics;
        ColorManager primaryColor;
        float fogStart;
        float fogEnd;
        float fogAlpha;
        Vec3d drag;
        float speed;
        FluidSplashParticleManager splashParticleManager = null;

        /**
         * The settings container for the custom fluid block.
         */
        private Settings(){
            this.useDefaultFluidPhysics = true;
            // Sets the default values for the fog.
            this.fogStart = -8.0F;
            this.fogEnd = 25.0F;
            this.fogAlpha = 0.5F;
        }

        /**
         * Creates a new settings container
         * @return The settings container
         */
        public static Settings create(){
            return new Settings();
        }

        /**
         * Will the custom fluid be using the default physics.
         * @param useDefault Will it be using the default
         * @return The container
         */
        public Settings useDefaultPhysics(boolean useDefault){
            this.useDefaultFluidPhysics = useDefault;
            return this;
        }

        /**
         * Checks to see if fluid container uses default physics
         * @return a boolean to check if it uses the default physics.
         */
        public boolean usesDefaultPhysics(){
            return this.useDefaultFluidPhysics;
        }

        /**
         * Sets the drag force of the custom fluid block.
         * @param drag The force of the drag
         * @return The container
         */
        public Settings setDrag(Vec3d drag){
            this.drag = drag;
            this.useDefaultPhysics(false);
            return this;
        }

        /**
         * Gets the drag of the custom fluid.
         * @return A Vec3d of the drag force.
         */
        public Vec3d getDrag(){
            return this.drag;
        }

        /**
         * Sets the primary color of the custom fluid block.
         * @param hex The color in hex format to use for the fluid block.
         * @return The container
         */
        public Settings setColor(int hex){
            this.primaryColor = new ColorManager(hex);
            return this;
        }

        /**
         * Gets the color of the fluid block.
         * @return Returns a color manager object with all the color information.
         */
        public ColorManager getColor(){
            return this.primaryColor;
        }

        /**
         * The renders view of the start of the fog. The fog directly in front of player.
         * @param fogStart The distance the fog starts.
         * @return The container.
         */
        public Settings setFogStart(float fogStart){
            this.fogStart = fogStart;
            return this;
        }

        /**
         * Gets the front of the fog, the start.
         * @return The distance.
         */
        public float getFogStart(){
            return this.fogStart;
        }

        /**
         * The render view of the end of the fog. The fog that is at the back of the start.
         * @param fogEnd The distance from the front.
         * @return The container.
         */
        public Settings setFogEnd(float fogEnd){
            this.fogEnd = fogEnd;
            return this;
        }

        /**
         * Gets the back of the fog, the end.
         * @return The distance.
         */
        public float getFogEnd(){
            return this.fogEnd;
        }

        /**
         * Sets the opacity (Transparency) of the fog.
         * @param alpha The opacity/alpha
         * @return The container
         */
        public Settings setFogAlpha(float alpha){
            this.fogAlpha = alpha;
            return this;
        }

        /**
         * Gets the fog's opacity (transparency).
         * @return The opacity/alpha.
         */
        public float getFogAlpha(){
            return this.fogAlpha;
        }

        /**
         * Sets the speed it will affect players with.
         * @param speed The speed to set players with
         * @return The container
         */
        public Settings setSpeed(float speed){
            this.speed = speed;
            this.useDefaultPhysics(false);
            return this;
        }

        /**
         * Gets the speed that the fluid block gives players.
         * @return A float of the speed.
         */
        public float getSpeed(){
            return this.speed;
        }

        /**
         * Sets the particles that will be used when entities interacts with the fluid block.
         * @param splashParticleManager The splash particle manager that stores all the particle effects used.
         * @return The container
         */
        public Settings setSplashParticles(FluidSplashParticleManager splashParticleManager){
            this.splashParticleManager = splashParticleManager;
            return this;
        }

        /**
         * Gets the fluid splash particle manager from the fluid block.
         * @return The manager.
         */
        public FluidSplashParticleManager getSplashParticles(){
            return this.splashParticleManager;
        }
    }

    public static class FluidSplashParticleManager {
        SimpleParticleType splashParticle;
        SimpleParticleType bubbleParticle;

        /**
         * Initialization of the manager
         * @param splashParticle The particle type used for when an entity jumps into a fluid block.
         * @param bubbleParticle The bubble particle type.
         */
        private FluidSplashParticleManager(SimpleParticleType splashParticle, SimpleParticleType bubbleParticle){
            this.splashParticle = splashParticle;
            this.bubbleParticle = bubbleParticle;
        }

        /**
         * Creates the container of the manager
         * @return The container
         */
        public static FluidSplashParticleManager create(){
            return new FluidSplashParticleManager(ParticleTypes.SPLASH, ParticleTypes.BUBBLE);
        }

        /**
         * This removes the default splash particles.
         * @return The container
         */
        public FluidSplashParticleManager removeDefaultSplashParticle(){
            this.splashParticle = null;
            return this;
        }

        /**
         * Removes the default bubble particle
         * @return The container
         */
        public FluidSplashParticleManager removeDefaultBubbleParticle(){
            this.bubbleParticle = null;
            return this;
        }

        /**
         * Remove all of the default particles.
         * @return The container
         */
        public FluidSplashParticleManager removeDefaultParticles(){
            this.removeDefaultSplashParticle().removeDefaultBubbleParticle();
            return this;
        }

        /**
         * Sets the particles for the fluid block.
         * @param splashParticle The particle type for the splash particles (When an entity jumps into a fluid block)
         * @param bubbleParticle The particle type for the bubble particles (When entities move through water or when fishing)
         * @return The container
         */
        public FluidSplashParticleManager setParticles(SimpleParticleType splashParticle, SimpleParticleType bubbleParticle){
            this.splashParticle = splashParticle;
            this.bubbleParticle = bubbleParticle;
            return this;
        }

        /**
         * Gets the splash particle type used for the fluid block.
         * @return The simple particle type
         */
        public SimpleParticleType  getSplashParticle(){
            return this.splashParticle;
        }

        /**
         * Gets the bubble particle type used for the fluid block.
         * @return The simple particle type
         */
        public SimpleParticleType getBubbleParticle(){
            return this.bubbleParticle;
        }
    }
}
