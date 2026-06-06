package io.github.com.group31.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.com.group31.component.Animation2D;
import io.github.com.group31.component.Animation2D.AnimationType;
import io.github.com.group31.component.Attack;
import io.github.com.group31.component.Damaged;
import io.github.com.group31.component.Dead;
import io.github.com.group31.component.Fsm;
import io.github.com.group31.component.Move;

public enum AnimationState implements State<Entity> {
    IDLE {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.IDLE);
        }

        @Override
        public void update(Entity entity) {
            if (Dead.MAPPER.has(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }

            Move move = Move.MAPPER.get(entity);
            if (move != null && !move.isRooted() && !move.getDirection().isZero()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(WALK);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack != null && attack.isAttacking()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(ATTACK);
                return;
            }

            Damaged damaged = Damaged.MAPPER.get(entity);
            if (damaged != null) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    WALK {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.WALK);
        }

        @Override
        public void update(Entity entity) {
            if (Dead.MAPPER.has(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }

            Move move = Move.MAPPER.get(entity);
            if (move.getDirection().isZero() || move.isRooted()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    ATTACK {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.ATTACK);
        }

        @Override
        public void update(Entity entity) {
            if (Dead.MAPPER.has(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack.canAttack()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    DAMAGED {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.DAMAGED);
        }

        @Override
        public void update(Entity entity) {
            if (Dead.MAPPER.has(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }

            Animation2D animation2D = Animation2D.MAPPER.get(entity);
            if (animation2D.isFinished()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    DEAD {
        @Override
        public void enter(Entity entity) {
            // Play dead animation once (falls back to DAMAGED if no dead frames in atlas)
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) {
                anim.setType(AnimationType.DEAD);
                anim.setPlayMode(Animation.PlayMode.NORMAL);
            }
            // Freeze movement permanently
            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.getDirection().setZero();
                move.setRooted(true);
            }
        }

        @Override
        public void update(Entity entity) {
            // Stay dead — DeadSystem will remove the entity after animation finishes
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    ROLL {
        @Override
        public void enter(Entity entity) {
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) {
                anim.setType(AnimationType.ROLL);
                anim.setPlayMode(Animation.PlayMode.NORMAL);
            }
            // Khoá di chuyển bình thường trong khi đang lướt
            Move move = Move.MAPPER.get(entity);
            if (move != null) move.setRooted(true);
        }

        @Override
        public void update(Entity entity) {
            if (Dead.MAPPER.has(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null && anim.isFinished()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
            Move move = Move.MAPPER.get(entity);
            if (move != null) move.setRooted(false);
            // Khôi phục play mode về LOOP cho các animation sau
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) anim.setPlayMode(Animation.PlayMode.LOOP);
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    }
}
