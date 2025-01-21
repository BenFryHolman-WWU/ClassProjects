#include <stdio.h>
#include <stdlib.h>
#include "bits.h"
#include "control.h"
#include "instruction.h"
#include "x16.h"
#include "trap.h"
#include "decode.h"


// Update condition code based on result
void update_cond(x16_t* machine, reg_t reg) {
    uint16_t result = x16_reg(machine, reg);
    if (result == 0) {
        x16_set(machine, R_COND, FL_ZRO);
    } else if (is_negative(result)) {
        x16_set(machine, R_COND, FL_NEG);
    } else {
        x16_set(machine, R_COND, FL_POS);
    }
}
// Execute a single instruction in the given X16 machine. Update
// memory and registers as required. PC is advanced as appropriate.
// Return 0 on success, or -1 if an error or HALT is encountered.
int execute_instruction(x16_t* machine) {
    // Fetch the instruction and advance the program counter
    uint16_t pc = x16_pc(machine);
    uint16_t instruction = x16_memread(machine, pc);
    x16_set(machine, R_PC, pc + 1);

    if (LOG) {
        fprintf(LOGFP, "0x%x: %s\n", pc, decode(instruction));
    }

    // Variables we might need in various instructions
    reg_t dst, src1, src2, base;
    uint16_t result, indirect, offset, imm, cond, jsrflag, op1, op2;

    // Decode the instruction
    uint16_t opcode = getopcode(instruction);
    switch (opcode) {
        case OP_ADD: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t srcReg1 = getbits(instruction, 6, 3);
            bool immediateFlag = getbit(instruction, 5);

            if (immediateFlag == 0) {
                uint16_t srcReg2 = getbits(instruction, 0, 3);
                uint16_t value1 = x16_reg(machine, srcReg1);
                uint16_t value2 = x16_reg(machine, srcReg2);
                uint16_t result = value1 + value2;
                x16_set(machine, dstReg, result);
            } else {
                uint16_t imm5 = sign_extend(getbits(instruction, 0, 5), 5);
                uint16_t value1 = x16_reg(machine, srcReg1);
                uint16_t result = value1 + imm5;
                x16_set(machine, dstReg, result);
            }

            update_cond(machine, dstReg);
            break;
        }
        case OP_AND: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t srcReg1 = getbits(instruction, 6, 3);
            bool immediateFlag = getbit(instruction, 5);

            if (immediateFlag == 0) {
                uint16_t srcReg2 = getbits(instruction, 0, 3);
                uint16_t value1 = x16_reg(machine, srcReg1);
                uint16_t value2 = x16_reg(machine, srcReg2);
                uint16_t result = value1 & value2;
                x16_set(machine, dstReg, result);
            } else {
                uint16_t imm5 = sign_extend(getbits(instruction, 0, 5), 5);
                uint16_t value1 = x16_reg(machine, srcReg1);
                uint16_t result = value1 & imm5;
                x16_set(machine, dstReg, result);
            }

            update_cond(machine, dstReg);
            break;
        }
        case OP_NOT: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t srcReg1 = getbits(instruction, 6, 3);
            uint16_t value1 = x16_reg(machine, srcReg1);
            uint16_t result = ~value1;
            x16_set(machine, dstReg, result);

            update_cond(machine, dstReg);
            break;
        }
        case OP_BR: {
            uint16_t condFlags = getbits(instruction, 9, 3);
            uint16_t currentCond = x16_reg(machine, R_COND);
            uint16_t pcOffset = sign_extend(getbits(instruction, 0, 9), 9);
            uint16_t target = x16_pc(machine) + pcOffset;
            if (condFlags == 0 || (condFlags & currentCond)) {
                x16_set(machine, R_PC, target);
            }
            break;
        }
        case OP_JMP: {
            uint16_t baseReg = getbits(instruction, 6, 3);
            x16_set(machine, R_PC, x16_reg(machine, baseReg));
            break;
        }
        case OP_JSR: {
            uint16_t longFlag = getbit(instruction, 11);
            uint16_t pc = x16_pc(machine);
            x16_set(machine, R_R7, pc);
            if (longFlag) {
                uint16_t longPcOffset =
                sign_extend(getbits(instruction, 0, 11), 11);
                x16_set(machine, R_PC, pc + longPcOffset);
            } else {
                uint16_t baseReg = getbits(instruction, 6, 3);
                x16_set(machine, R_PC, x16_reg(machine, baseReg));
            }
            break;
        }
        case OP_LD: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t pcOffset = sign_extend(getbits(instruction, 0, 9), 9);
            uint16_t address = x16_pc(machine) + pcOffset;
            uint16_t value = x16_memread(machine, address);
            x16_set(machine, dstReg, value);
            update_cond(machine, dstReg);
            break;
        }
        case OP_LDI: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t pcOffset = sign_extend(getbits(instruction, 0, 9), 9);
            uint16_t address = x16_memread(machine, x16_pc(machine) + pcOffset);
            uint16_t value = x16_memread(machine, address);
            x16_set(machine, dstReg, value);
            update_cond(machine, dstReg);
            break;
        }
        case OP_LDR: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t baseReg = getbits(instruction, 6, 3);
            uint16_t offset = sign_extend(getbits(instruction, 0, 6), 6);
            uint16_t address = x16_reg(machine, baseReg) + offset;
            uint16_t value = x16_memread(machine, address);
            x16_set(machine, dstReg, value);
            update_cond(machine, dstReg);
            break;
        }
        case OP_LEA: {
            uint16_t dstReg = getbits(instruction, 9, 3);
            uint16_t pcOffset = sign_extend(getbits(instruction, 0, 9), 9);
            x16_set(machine, dstReg, x16_pc(machine) + pcOffset);
            update_cond(machine, dstReg);
            break;
        }
        case OP_ST: {
            uint16_t srcReg = getbits(instruction, 9, 3);
            uint16_t pcOffset = sign_extend(getbits(instruction, 0, 9), 9);
            uint16_t address = x16_pc(machine) + pcOffset;
            x16_memwrite(machine, address, x16_reg(machine, srcReg));
            break;
        }
        case OP_STI: {
            uint16_t srcReg = getbits(instruction, 9, 3);
            uint16_t pcOffset = sign_extend(getbits(instruction, 0, 9), 9);
            uint16_t address = x16_memread(machine, x16_pc(machine) + pcOffset);
            x16_memwrite(machine, address, x16_reg(machine, srcReg));
            break;
        }
        case OP_STR: {
            uint16_t srcReg = getbits(instruction, 9, 3);
            uint16_t baseReg = getbits(instruction, 6, 3);
            uint16_t offset = sign_extend(getbits(instruction, 0, 6), 6);
            uint16_t address = x16_reg(machine, baseReg) + offset;
            x16_memwrite(machine, address, x16_reg(machine, srcReg));
            break;
        }
        case OP_TRAP:
            // Execute the trap -- do not rewrite
            return trap(machine, instruction);

        case OP_RES:
        case OP_RTI:
        default:
            // Bad codes, never used
            abort();
    }

    return 0;
}
