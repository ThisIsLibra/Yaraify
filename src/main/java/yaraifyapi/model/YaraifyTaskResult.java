/*
 * Copyright (C) 2022 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package yaraifyapi.model;

import java.util.List;

/**
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class YaraifyTaskResult {

    
    private YaraifyMetadata metadata;
    private List<YaraifyTask> tasks;

    public YaraifyTaskResult(YaraifyMetadata metadata, List<YaraifyTask> tasks) {
        this.metadata = metadata;
        this.tasks = tasks;
    }

    public YaraifyMetadata getMetadata() {
        return metadata;
    }

    public List<YaraifyTask> getTasks() {
        return tasks;
    }
}
