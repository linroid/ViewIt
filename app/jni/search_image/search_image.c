#include <unistd.h>
#include <sys/types.h>
#include <dirent.h>
#include <stdio.h>
#include <memory.h>
#include <stdint.h>
#include <sys/stat.h>
#include <stdlib.h>

typedef enum ImageType {
    GIF,
    JPEG,
    PNG_A,
    PNG,
    UNKNOWN = -1
} ImageType;

ImageType get_image_type(const char *path);

uint16_t readUInt16(FILE *pFILE);

uint8_t readUInt8(FILE *pFILE);

const int GIF_HEADER = 0x474946;
const int PNG_HEADER = 0x89504E47;
const uint16_t EXIF_MAGIC_NUMBER = 0xFFD8;

void search_image(const char *name) {
//    printf("search_image:%s\n", name);
    DIR *dir;
    struct dirent *entry;
    struct stat statbuf;

    if (!(dir = opendir(name))) {
        return;
    }
    while ((entry = readdir(dir))) {
        char path[1024];
        int len = snprintf(path, sizeof(path) - 1, "%s/%s", name, entry->d_name);
        path[len] = 0;
        if (entry->d_type == DT_DIR) {
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0)
                continue;
            search_image(path);
        } else if (entry->d_type == DT_REG) {
            ImageType type = get_image_type(path);
            if (type != UNKNOWN) {
                lstat(path, &statbuf);
                printf("%lli %lli %d %s\n", statbuf.st_size, statbuf.st_mtime, type, path);
            }
        }
    }
    closedir(dir);
}

uint16_t readUInt16(FILE *fp) {
    uint16_t arr[1];
    fread(arr, sizeof(uint16_t), 1, fp);
    return htons(arr[0]);
}

uint8_t readUInt8(FILE *fp) {
    uint8_t arr[1];
    fread(arr, sizeof(uint8_t), 1, fp);
    return arr[0];
}

ImageType get_image_type(const char *path) {
    FILE *fp;
    ImageType type = UNKNOWN;
    if ((fp = fopen(path, "r")) != NULL) {

        int firstTwoBytes = readUInt16(fp);
        if (firstTwoBytes == EXIF_MAGIC_NUMBER) {
            type = JPEG;
        } else {
            int firstFourBytes = firstTwoBytes << 16 & 0xFFFF0000 | readUInt16(fp) & 0xFFFF;
            // PNG.
            if (firstFourBytes == PNG_HEADER) {
                // See: http://stackoverflow.com/questions/2057923/how-to-check-a-png-for-grayscale-alpha-color-type
                fseek(fp, 25 - 4, SEEK_CUR);
                int alpha = readUInt8(fp);
                // A RGB indexed PNG can also have transparency. Better safe than sorry!
                type = alpha >= 3 ? PNG_A : PNG;
            } else {
                // GIF from first 3 bytes.
                if (firstFourBytes >> 8 == GIF_HEADER) {
                    type = GIF;
                }
            }
        }
        fclose(fp);
    }
    return type;
}

int main(int argc, char *argv[]) {
    search_image(argv[1]);
//    printf("finished!\n");
    exit(0);
}