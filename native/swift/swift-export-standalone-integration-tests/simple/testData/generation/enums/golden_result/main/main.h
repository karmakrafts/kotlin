#include <Foundation/Foundation.h>
#include <stdint.h>

void * Enum_a_get();

void * Enum_b_get();

NSArray<id> * Enum_entries_get();

int32_t Enum_i_get(void * self);

void Enum_i_set__TypesOfArguments__Swift_Int32__(void * self, int32_t newValue);

NSString * Enum_print(void * self);

void * Enum_valueOf__TypesOfArguments__Swift_String__(NSString * value);
