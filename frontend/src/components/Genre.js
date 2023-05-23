import React, { Component } from "react";
export default class Genre extends Component {
    constructor(props) {
        super(props);
        this.state = {
            genre: [],
        };
    }

    async componentDidMount() {
        const url = "http://localhost:3002/3";
        const response = await fetch(url);
        const data = await response.json();
        console.log(data);
        this.setState({ genre: data });
    }

    

    render() {
        return (
            <div style={{ display: "block", marginTop: "200px" }}>
                <h1>Genre Matching With You </h1>
                {
                    this.state.genre.map((obj,index) => {
                        return <div key={index}>
                            <h2>{obj.genre.value}</h2>
                        </div>
                    }
                    )
                }

            </div>
        );
    }
}