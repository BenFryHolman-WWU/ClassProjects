#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <arpa/inet.h>
#include <ctype.h>
#include "instruction.h"
#include "x16.h"
#include "trap.h"

void writeBigEndian(FILE *file, uint16_t value) {
    fputc((value >> 8) & 0xFF, file);
    fputc(value & 0xFF, file);
}

typedef struct {
    char label[50];
    int address;
} Symbol;

reg_t Reg_t;
Symbol symbols[10000];
int symbolCount = 0;

void process_line(char* line) {
    char* end;
    char* start = line;

    line[strcspn(line, "\r\n")] = 0;
    while (isspace((unsigned char)*start)){
        start++;
    }
    if (*start == 0) {
        *line = 0;
        return;
    }
    end = start + strlen(start) - 1;
    while (end > start && isspace((unsigned char)*end)) {
        end--;
    }
    *(end+1) = 0;
    memmove(line, start, end - start + 2);
    char* comment = strchr(line, '#');
    if (comment){
        *comment = '\0';
    }
}

reg_t parse_register(const char* reg) {
    if (reg[0] == '%' && reg[1] == 'r') {
        int regNum = atoi(&reg[2]);
        switch (regNum) {
            case 0: return R_R0;
            case 1: return R_R1;
            case 2: return R_R2;
            case 3: return R_R3;
            case 4: return R_R4;
            case 5: return R_R5;
            case 6: return R_R6;
            case 7: return R_R7;
        }
        return (reg_t)regNum;
    }
}

int findLabelAddress(const char* label) {
    for (int i = 0; i < symbolCount; ++i) {
        if (strcmp(symbols[i].label, label) == 0) {
            return symbols[i].address;
        }
    }
    return -1;
}

int calculate_offset(int currentAddress, int labelAddress) {
    return (labelAddress - (currentAddress + 2)) / 2;
}


uint16_t parse_instruction(const char* line, int currentAddress) {
    char instruction[10], reg1[10], reg2[10], reg3[10], label[50];
    int immediate, offset;
    uint16_t parsedInstruction = 0;
    if (line == NULL || strlen(line) == 0) {
        fprintf(stderr, "Empty or null instruction line.\n");
        return 2;
    }
    if (strncmp(line, "br", 2) == 0) {
        bool neg = false, zero = false, pos = false;
        char conditions[5];
        int scanCount = sscanf(line, "br%[nzp]", conditions);
        if (scanCount == 1) {
            if (strchr(conditions, 'n') != NULL) neg = true;
            if (strchr(conditions, 'z') != NULL) zero = true;
            if (strchr(conditions, 'p') != NULL) pos = true;
        }
        char* afterConditions = line + 2 + strlen(conditions);
        int offsetValue;
        if (sscanf(afterConditions, "%d", &offsetValue) == 1) {
        } else {
            char label[50];
            sscanf(afterConditions, "%s", label);
            int labelAddress = findLabelAddress(label);
            if (labelAddress == -1) {
                fprintf(stderr, "Label not found: %s\n", label);
                exit(2);
            }
            offsetValue = calculate_offset(currentAddress, labelAddress);
        }
        parsedInstruction = emit_br(neg, zero, pos, offsetValue);
    } else if (sscanf(line, "jmp %s", reg1) == 1) {
        parsedInstruction = emit_jmp(parse_register(reg1));
    } else if (sscanf(line, "jsr %s", label) == 1) {
        offset = calculate_offset(currentAddress, findLabelAddress(label));
        parsedInstruction = emit_jsr(offset);
    } else if (sscanf(line, "jssr %s", reg1) == 1) {
        parsedInstruction = emit_jsrr(parse_register(reg1));
    } else if (sscanf(line, "ld %s %s", reg1, label) == 2) {
        offset = calculate_offset(currentAddress, findLabelAddress(label));
        parsedInstruction = emit_ld(parse_register(reg1), offset);
    } else if (sscanf(line, "ldi %s %s", reg1, label) == 2) {
        offset = calculate_offset(currentAddress, findLabelAddress(label));
        parsedInstruction = emit_ldi(parse_register(reg1), offset);
    } else if (sscanf(line, "ldr %s %s #%d", reg1, reg2, &immediate) == 3) {
        parsedInstruction = emit_ldr(parse_register(reg1),
        parse_register(reg2), immediate);
    } else if (sscanf(line, "lea %s %s", reg1, label) == 2) {
        offset = calculate_offset(currentAddress, findLabelAddress(label));
        parsedInstruction = emit_lea(parse_register(reg1), offset);
    } else if (sscanf(line, "st %s %s", reg1, label) == 2) {
        offset = calculate_offset(currentAddress, findLabelAddress(label));
        parsedInstruction = emit_st(parse_register(reg1), offset);
    } else if (sscanf(line, "sti %s %s", reg1, label) == 2) {
        offset = calculate_offset(currentAddress, findLabelAddress(label));
        parsedInstruction = emit_sti(parse_register(reg1), offset);
    } else if (sscanf(line, "str %s %s #%d", reg1, reg2, &immediate) == 3) {
        parsedInstruction = emit_str(parse_register(reg1),
        parse_register(reg2), immediate);
    } else if (sscanf(line, "add %s %s %s", reg1, reg2, reg3) == 3) {
        if (reg3[0] == '$') {
            immediate = atoi(reg3 + 1);
            parsedInstruction = emit_add_imm(parse_register(reg1),
            parse_register(reg2), immediate);
        } else {
            parsedInstruction = emit_add_reg(parse_register(reg1),
            parse_register(reg2), parse_register(reg3));
        }
    } else if (sscanf(line, "and %s %s %s", reg1, reg2, reg3) == 3) {
        if (reg3[0] == '$') {
            immediate = atoi(reg3 + 1);
            parsedInstruction = emit_and_imm(parse_register(reg1),
            parse_register(reg2), immediate);
        } else {
            parsedInstruction = emit_and_reg(parse_register(reg1),
            parse_register(reg2), parse_register(reg3));
        }
    } else if (sscanf(line, "add %s %s %s", reg1, reg2, reg3) == 3) {
        if (reg3[0] == '$') {
            immediate = atoi(reg3 + 1);
            parsedInstruction = emit_add_imm(parse_register(reg1),
            parse_register(reg2), immediate);
        } else {
            parsedInstruction = emit_add_reg(parse_register(reg1),
            parse_register(reg2), parse_register(reg3));
        }
    } else if (sscanf(line, "not %s %s", reg1, reg2) == 2) {
        parsedInstruction = emit_not(parse_register(reg1),
        parse_register(reg2));
    } else if (sscanf(line, "val $%d", &immediate) == 1) {
        uint16_t value = (uint16_t)immediate;
        parsedInstruction = emit_value(value);
    } else if (strcmp(line, "getc") == 0) {
        parsedInstruction = 0xF000 | TRAP_GETC;
    } else if (strcmp(line, "putc") == 0) {
        parsedInstruction = 0xF000 | TRAP_OUT;
    } else if (strcmp(line, "puts") == 0) {
        parsedInstruction = 0xF000 | TRAP_PUTS;
    } else if (strcmp(line, "enter") == 0) {
        parsedInstruction = 0xF000 | TRAP_IN;
    } else if (strcmp(line, "putsp") == 0) {
        parsedInstruction = 0xF000 | TRAP_PUTSP;
    } else if (strcmp(line, "halt") == 0) {
        parsedInstruction = 0xF000 | TRAP_HALT;
    } else {
        fprintf(stderr, "Unrecognized instruction: %s\n", line);
        return 2;
    }
    return parsedInstruction;
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Usage: ./xas filename\n");
        return 1;
    }
    FILE *sourceFile = fopen(argv[1], "r");
    char line[10000];
    int currentAddress = 0x3000;
    while (fgets(line, sizeof(line), sourceFile)) {
        process_line(line);
        char* colonPos = strchr(line, ':');
        if (colonPos) {
            *colonPos = '\0';
            strcpy(symbols[symbolCount].label, line);
            symbols[symbolCount].address = currentAddress;
            symbolCount++;
        } else if (strlen(line) > 1) {
            currentAddress += 2;
        }
    }

    rewind(sourceFile);
    FILE *outputFile = fopen("a.obj", "wb");
    writeBigEndian(outputFile, 0x3000);
    currentAddress = 0x3000;
    while (fgets(line, sizeof(line), sourceFile)) {
        process_line(line);
        if (strlen(line) > 1 && strchr(line, ':') == NULL) {
            uint16_t instruction = parse_instruction(line, currentAddress);
            writeBigEndian(outputFile, instruction);
            currentAddress += 2;
        }
    }
    fclose(sourceFile);
    fclose(outputFile);
    return 0;
}
