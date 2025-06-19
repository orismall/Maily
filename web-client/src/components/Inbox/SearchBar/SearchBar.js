import React, { useState } from 'react';
import { FaSearch, FaTimes } from 'react-icons/fa';
import './SearchBar.css';

const SearchBar = ({ onSearch, inputRef, onClearSearch, setSelectedMail, setCurrentFolder }) => {
  // State to control visibility of the clear (X) button
  const [showClear, setShowClear] = useState(false);

  // Triggered when the user clicks the search icon or presses Enter
  const handleSearch = () => {
    if (inputRef && inputRef.current) {
      const value = inputRef.current.value.trim(); 
      setShowClear(!!value); 

      if (value) {
        // Reset selected mail and folder view
        setSelectedMail(null);
        setCurrentFolder(null);

        // Trigger search callback with the entered value
        onSearch(value);
      }
    }
  };

  // Triggered when the user clicks the clear (X) icon
  const handleClear = () => {
    if (inputRef && inputRef.current) {
      inputRef.current.value = ""; 
      setShowClear(false);         
      onClearSearch();             
    }
  };

  // Triggered on keyboard events; submits search on Enter key
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // Triggered when the input value changes; toggles the clear button
  const handleInputChange = () => {
    const value = inputRef.current?.value.trim();
    setShowClear(!!value); // Show clear if text exists
  };

  return (
    <div className="search-bar-wrapper">
      {/* Search icon triggers handleSearch */}
      <FaSearch
        className="search-icon"
        title="Search button"
        onClick={handleSearch}
      />

      <div className="input-wrapper">
        {/* Input field for search query */}
        <input
          type="text"
          placeholder="Search mail"
          className="search-input"
          ref={inputRef}
          onKeyDown={handleKeyDown}
          onChange={handleInputChange}
        />

        {/* Clear (X) icon appears only if input is non-empty */}
        {showClear && (
          <FaTimes
            className="clear-icon"
            title="Clear search"
            onClick={handleClear}
          />
        )}
      </div>
    </div>
  );
};

export default SearchBar;
