import sys
from datetime import datetime
from os import path
import os
import argparse
import random

universities = ['MSU', 'NRU HSE', 'MIPT',
                'RUDN University', 'BMSTU',
                'NRNU MEPhI', 'MSIIR', 'MMA',
                'MSPU', 'PRUE', 'PRUE',
                'Synergy University', 'University of laziness',
                'University of Something', 'University One More',
                'University of Eggheads']


def generatorPersons():
    persons = {}
    people = []
    for unik in universities:
        unik_persons = []
        for i in range(3000):
            id_person = random.randint(1000000, 9999999)
            if id_person in people:
                id_person = random.randint(1000000, 9999999)
            unik_persons.append(id_person)
            people.append(id_person)
        persons[unik] = unik_persons
    return persons


def generatorJournalAttendanceAndPublications(persons, options, startTime, endTime, iterations):
    # - журнал посещаемости:
    # 		университет,
    #		идентификатор сотрудника/студента,
    # 		время-дата,
    # 		тип: вход или выход
    # - реестр публикаций:
    # 		университет,
    # 		авторы (идентификатор сотрудника/студента),
    # 		id публикации,
    # 		дата
    records = list()
    records.append(startTime)
    for i in range(iterations):
        records.append(records[i] + 10)
    records.append(endTime)

    file_publications = open(path.abspath(path.join(options.output, "publications.txt")), 'w+')
    with open(path.abspath(path.join(options.output, "attendances.txt")), 'w+') as file_attendance:
        count = 0
        entered_person, pubs = [], []
        for i in range(iterations):

            if count < 4321:
                for j in range(2):
                    unik = random.choice(universities)
                    index = random.randint(0, 2999)
                    while [persons[unik][index], unik] in entered_person:
                        index = random.randint(0, 2999)

                    file_attendance.write(str(datetime.fromtimestamp(records[i])) + '|' + unik + '|'
                                          + str(persons[unik][index]) + '|' + str(-1) + "\n")

                    if random.randint(0, 2) == 2:
                        id_pub = random.randint(100000000, 999999999)
                        while id_pub in pubs:
                            id_pub = random.randint(100000000, 999999999)
                        file_publications.write(str(datetime.fromtimestamp(records[i])) + '|' + unik + '|'
                                                + str(persons[unik][index]) + '|' + str(id_pub) + "\n")

                    entered = [persons[unik][index], unik]
                    entered_person.append(entered)
            else:
                for j in range(2):
                    unik = entered_person[0][1]
                    person = entered_person[0][0]
                    file_attendance.write(str(datetime.fromtimestamp(records[i])) + '|' + unik + '|'
                                          + str(person) + '|' + str(1) + "\n")
                    entered_person = entered_person[1:]

            count += 1
            if count == 8642:
                count = 0
                entered_person = []
                pubs = []
    file_publications.close()


def main():
    parser = argparse.ArgumentParser(description="Generating data of the publication activity"
                                                 "\nof university employees and students"
                                                 "\nand statistics on the organization's attendance for the year")
    parser.add_argument('--start-date', type=str,
                        help='Start date: dd.mm.yyyy')
    parser.add_argument('--end-date', type=str,
                        help='End date: dd.mm.yyyy')
    parser.add_argument('--output', type=str,
                        help='Output directory', default='../input_data')

    options = parser.parse_args()

    if not options.start_date or not options.start_date:
        print("\n\n", parser.description, "\nSome parameters are not entered correctly."
                                          "\n\nFor more information use '--help'\n")
        sys.exit(1)

    startTime = int(datetime.strptime(options.start_date + ' 00:00:00', '%d.%m.%Y %H:%M:%S').timestamp())
    endTime = int(datetime.strptime(options.end_date + ' 23:59:59', '%d.%m.%Y %H:%M:%S').timestamp())

    if startTime > endTime:
        print(parser.description, "\nParameters of dates are not entered correctly."
                                  "\n\nFor more information use '--help'\n")
        sys.exit(2)

    if not os.path.isdir(path.abspath(options.output)):
        print("Default or your directory is not exist! I create it!)")
        os.mkdir(path.abspath(options.output))

    interval = endTime - startTime
    iterations = interval // 10

    persons = generatorPersons()

    generatorJournalAttendanceAndPublications(persons, options, startTime, endTime, iterations)


if __name__ == '__main__':
    main()
